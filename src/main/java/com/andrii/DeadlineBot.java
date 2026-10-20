package com.andrii;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class DeadlineBot implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final DeadlineStore store = new DeadlineStore();

    public DeadlineBot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
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
