package com.andrii;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeadlineBot implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final DeadlineStore store = new DeadlineStore();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public DeadlineBot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        scheduler.scheduleAtFixedRate(this::sendReminders, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        if ("/ping".equals(text)) {
            sendText(chatId, "pong");
        } else if (text.startsWith("/add")) {
            try {
                Deadline deadline = DeadlineParser.parse(text);
                store.add(chatId, deadline);
                sendText(chatId, "Added: " + deadline.getTitle());
            } catch (IllegalArgumentException e) {
                sendText(chatId, e.getMessage());
            }
        } else if ("/list".equals(text)) {
            List<Deadline> deadlines = store.getAll(chatId);
            if (deadlines.isEmpty()) {
                sendText(chatId, "No deadlines yet.");
                return;
            }
            StringBuilder sb = new StringBuilder("Your deadlines:\n");
            for (Deadline d : deadlines) {
                sb.append(d.getCourse())
                        .append(" — ")
                        .append(d.getTitle())
                        .append(" — ")
                        .append(d.getDueAt().format(DeadlineParser.FORMAT))
                        .append("\n");
            }
            sendText(chatId, sb.toString());
        }
    }

    private void sendReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Reminder> reminders = store.findDeadlinesToRemind(now, now.plusHours(24));

            for (Reminder r : reminders) {
                Deadline d = r.deadline();
                String text = "Reminder: " + d.getCourse() + " — " + d.getTitle()
                        + " is due " + d.getDueAt().format(DeadlineParser.FORMAT);

                sendText(r.chatId(), text);
                store.markReminded(r.id());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendText(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
