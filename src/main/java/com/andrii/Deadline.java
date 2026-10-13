package com.andrii;

import java.time.LocalDateTime;

public class Deadline {
    private final String course, title;
    private final LocalDateTime dueAt;


    public Deadline(String course, String title, LocalDateTime dueAt) {
        this.course = course;
        this.title = title;
        this.dueAt = dueAt;
    }

    public String getCourse() {
        return course;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }
}
