package com.deepbi.llm;

import reactor.core.publisher.Mono;

public interface LlmClient {
    Mono<String> chat(String userMessage);

    Mono<String> chat(String systemPrompt, String userMessage);
}
