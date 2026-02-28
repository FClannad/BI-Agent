package com.deepbi.web;

import com.deepbi.agent.ChatService;
import com.deepbi.web.dto.ChatEvent;
import com.deepbi.web.dto.ChatRequest;
import com.deepbi.web.dto.ChatResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public Mono<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request);
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(@RequestParam("message") String message,
                                                    @RequestParam(value = "sessionId", required = false) String sessionId) {
        ChatRequest request = new ChatRequest(message, sessionId);
        return chatService.chatStream(request)
                .map(event -> ServerSentEvent.builder(event.data())
                        .event(event.type())
                        .build());
    }
}
