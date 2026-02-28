package com.deepbi.llm.openai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenAiChatResponse(
        List<Choice> choices
) {
    public record Choice(
            Message message,
            @JsonProperty("finish_reason") String finishReason
    ) {
    }

    public record Message(
            String role,
            String content
    ) {
    }
}

