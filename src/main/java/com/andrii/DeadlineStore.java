package com.andrii;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeadlineStore {
    private final Map<Long, List<Deadline>> deadlines = new HashMap<>();
    public void add(long chatId, Deadline deadline) {
        if (!deadlines.containsKey(chatId)) {
            deadlines.put(chatId, new ArrayList<>());
        }
        deadlines.get(chatId).add(deadline);
    }
    public List<Deadline> getAll(long chatId) {
        return deadlines.getOrDefault(chatId, List.of());
    }
}
