package com.andrii;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class DeadlineStore {
    private static final String DB_URL = "jdbc:sqlite:deadlines.db";

    public DeadlineStore() {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS deadlines (
                        id      INTEGER PRIMARY KEY AUTOINCREMENT,
                        chat_id INTEGER NOT NULL,
                        course  TEXT NOT NULL,
                        title   TEXT NOT NULL,
                        due_at  TEXT NOT NULL,
                        reminded INTEGER NOT NULL DEFAULT 0                                                        
                    )
                    """);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create the deadlines table", e);
        }
    }

    public void add(long chatId, Deadline deadline) {
        String sql = "INSERT INTO deadlines (chat_id, course, title, due_at) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, chatId);
            statement.setString(2, deadline.getCourse());
            statement.setString(3, deadline.getTitle());
            statement.setString(4, deadline.getDueAt().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save the deadline", e);
        }
    }

    public void markReminded(long id) {
        String sql = "UPDATE deadlines SET reminded = 1 WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not mark the deadline as reminded", e);
        }
    }

    public List<Deadline> getAll(long chatId) {
        String sql = "SELECT course, title, due_at FROM deadlines WHERE chat_id = ? ORDER BY due_at";
        List<Deadline> result = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, chatId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    String course = rows.getString("course");
                    String title = rows.getString("title");
                    LocalDateTime dueAt = LocalDateTime.parse(rows.getString("due_at"));
                    result.add(new Deadline(course, title, dueAt));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load deadlines", e);
        }
        return result;
    }
}