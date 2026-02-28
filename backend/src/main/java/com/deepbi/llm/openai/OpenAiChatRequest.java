package com.deepbi.llm.openai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenAiChatRequest(
        String model,
        List<Message> messages,
        @JsonProperty("temperature") Double temperature
) {
    public record Message(
            String role,
            String content
    ) {
    }
}

