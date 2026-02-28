package com.deepbi.llm.openai;

import com.deepbi.llm.LlmClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class OpenAiClient implements LlmClient {
    private final WebClient webClient;
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    public OpenAiClient(
            WebClient webClient,
            @Value("${deepbi.openai.base-url}") String baseUrl,
            @Value("${deepbi.openai.api-key}") String apiKey,
            @Value("${deepbi.openai.model}") String model
    ) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public Mono<String> chat(String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            return Mono.error(new IllegalStateException("Missing OPENAI_API_KEY (or deepbi.openai.api-key)."));
        }

        OpenAiChatRequest request = new OpenAiChatRequest(
                model,
                List.of(
                        new OpenAiChatRequest.Message("system",
                                "You are DeepBI, a BI assistant. Be concise. If the user asks for data, explain what SQL you would run and what chart fits."),
                        new OpenAiChatRequest.Message("user", userMessage)
                ),
                0.2
        );

        return webClient
                .post()
                .uri(baseUrl + "/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(body -> Mono.error(new IllegalStateException("OpenAI error: " + resp.statusCode() + " " + body))))
                .bodyToMono(OpenAiChatResponse.class)
                .map(this::extractText);
    }

    private String extractText(OpenAiChatResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            return "";
        }
        OpenAiChatResponse.Choice first = response.choices().get(0);
        if (first == null || first.message() == null || first.message().content() == null) {
            return "";
        }
        return first.message().content();
    }
}
