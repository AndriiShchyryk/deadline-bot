package com.andrii;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

public class DeadlineParser {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static Deadline parse(String text) {
        String[] parts = text.trim().split("\\s+");
        if (parts.length < 5) {
            throw new IllegalArgumentException("Format: /add <course> <title> <dd.MM.yyyy> <HH:mm>");
        }
        String course = parts[1];
        String dateText = parts[parts.length - 2];
        String timeText = parts[parts.length - 1];

        String title = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length - 2));

        LocalDateTime dueAt;
        try {
            dueAt = LocalDateTime.parse(dateText + " " + timeText, FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date or time. Use dd.MM.yyyy HH:mm, e.g. 20.10.2026 23:59");
        }

        return new Deadline(course, title, dueAt);
    }
}
