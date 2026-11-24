package com.andrii;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class DeadlineParserTest {

    @Test
    void parsesValidInput() {
        String input = "/add CS355 Assignment 20.12.2026 23:59";

        Deadline d = DeadlineParser.parse(input);

        assertEquals("CS355", d.getCourse());
        assertEquals("Assignment", d.getTitle());
        assertEquals(LocalDateTime.of(2026, 12, 20, 23, 59), d.getDueAt());
    }

    @Test
    void parsesMultiWordTitle() {
        String input = "/add CS355 Assignment 2 Part B 20.12.2026 23:59";

        Deadline d = DeadlineParser.parse(input);

        assertEquals("CS355", d.getCourse());
        assertEquals("Assignment 2 Part B", d.getTitle());
        assertEquals(LocalDateTime.of(2026, 12, 20, 23, 59), d.getDueAt());

    }

    @Test
    void throwsWhenTooFewParts() {
        String input = "/add CS355 20.12.2026";

        assertThrows(IllegalArgumentException.class, () -> DeadlineParser.parse(input));
    }

    @Test
    void throwsOnInvalidDate() {
        String input = "/add CS355 Essay 32.12.2026 23:59";

        assertThrows(IllegalArgumentException.class, () -> DeadlineParser.parse(input));
    }

    @Test
    void throwsOnWrongDateFormat() {
        String input = "/add CS355 Essay 2026-12-20 23:59";

        assertThrows(IllegalArgumentException.class, () -> DeadlineParser.parse(input));
    }

    @Test
    void throwsOnNonExistentDate() {
        String input = "/add CS355 Essay 31.02.2026 23:59";

        assertThrows(IllegalArgumentException.class, () -> DeadlineParser.parse(input));
    }
}