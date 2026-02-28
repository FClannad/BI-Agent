package com.deepbi.agent;

import com.deepbi.llm.LlmClient;
import com.deepbi.web.dto.ChatEvent;
import com.deepbi.web.dto.ChatRequest;
import com.deepbi.web.dto.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {
    private final LlmClient llmClient;

    public ChatService(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public Mono<ChatResponse> chat(ChatRequest request) {
        return llmClient.chat(request.message())
                .map(text -> new ChatResponse(text, null, null));
    }

    public Flux<ChatEvent> chatStream(ChatRequest request) {
        return llmClient.chat(request.message())
                .flatMapMany(fullText -> {
                    List<ChatEvent> events = new ArrayList<>();
                    events.add(ChatEvent.start());

                    int chunkSize = 24;
                    for (int i = 0; i < fullText.length(); i += chunkSize) {
                        int end = Math.min(i + chunkSize, fullText.length());
                        events.add(ChatEvent.token(fullText.substring(i, end)));
                    }

                    events.add(ChatEvent.done());
                    return Flux.fromIterable(events);
                });
    }
}

