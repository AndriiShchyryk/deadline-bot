package com.andrii;

import java.util.*;

public class DeadlineStore {
    private final Map<Long, List<Deadline>> deadlines = new HashMap<>();
    public void add(long chatId, Deadline deadline) {
        if (!deadlines.containsKey(chatId)) {
            deadlines.put(chatId, new ArrayList<>());
        }
        deadlines.get(chatId).add(deadline);
    }
    public List<Deadline> getAll(long chatId) {
        List<Deadline> list = deadlines.getOrDefault(chatId, List.of());
        List<Deadline> copy = new ArrayList<>(list);
        copy.sort(Comparator.comparing(Deadline::getDueAt));
        return copy;
    }
}
