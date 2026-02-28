package com.deepbi.web.dto;

public record ChatEvent(
        String type,
        String data
) {
    public static ChatEvent start() {
        return new ChatEvent("start", "");
    }

    public static ChatEvent token(String token) {
        return new ChatEvent("token", token);
    }

    public static ChatEvent done() {
        return new ChatEvent("done", "");
    }
}

