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
    private static final String HELP_TEXT = "This bot helps you track your university deadlines.\n" +
            "Commands:\n" +
            "- /add — add a new deadline (Format: <course> <title> <dd.MM.yyyy> <HH:mm>)\n" +
            "Example: /add CS355 Assignment 2 20.12.2026 23:59\n" +
            "- /list — see all your deadlines, sorted by date\n" +
            "- /ping — check that the bot is running\n" +
            "- /delete <number> — delete a deadline by its number from /list\n" +
            "- Automatic reminders 24 hours and 1 hour before each deadline\n";
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
            int i = 1;
            for (Deadline d : deadlines) {
                sb.append(i)
                        .append(". ")
                        .append(d.getCourse())
                        .append(" — ")
                        .append(d.getTitle())
                        .append(" — ")
                        .append(d.getDueAt().format(DeadlineParser.FORMAT))
                        .append("\n");
                i++;
            }
            sendText(chatId, sb.toString());
        } else if ("/start".equals(text) || "/help".equals(text)) {
            sendText(chatId, HELP_TEXT);
        } else if (text.startsWith("/delete")) {
            String[] parts = text.split("\\s+");
            if (parts.length != 2) {
                sendText(chatId, "Format: /delete <number>");
                return;
            }
            try {
                int position = Integer.parseInt(parts[1]);
                if (position < 1) {
                    sendText(chatId, "Format: /delete <number>");
                    return;
                }
                if (store.delete(chatId, position)) {
                    sendText(chatId, "Deleted.");
                } else {
                    sendText(chatId, "No deadline with number " + position + ". Use /list to see numbers.");
                }
            } catch (NumberFormatException e) {
                sendText(chatId, "Format: /delete <number>");
            }
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

            List<Reminder> remindersHour = store.findDeadlinesToRemindHour(now, now.plusHours(1));

            for (Reminder r : remindersHour) {
                Deadline d = r.deadline();
                String text = "Reminder: " + d.getCourse() + " — " + d.getTitle()
                        + " is due in one hour (" + d.getDueAt().format(DeadlineParser.FORMAT) + ")";

                sendText(r.chatId(), text);
                store.markRemindedHour(r.id());
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
