package com.andrii;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {
    public static void main(String[] args) {
        String token = System.getenv("BOT_TOKEN");
        if (token == null) {
            System.err.println("The BOT_TOKEN variable is not set.");
            return;
        }

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(token, new DeadlineBot(token));
            System.out.println("Bot started");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}