package com.deepbi.web;

import com.deepbi.bi.BiAskRequest;
import com.deepbi.bi.BiAskResponse;
import com.deepbi.bi.BiAskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/bi")
public class BiController {
    private final BiAskService biAskService;

    public BiController(BiAskService biAskService) {
        this.biAskService = biAskService;
    }

    @PostMapping("/ask")
    public Mono<BiAskResponse> ask(@Valid @RequestBody BiAskRequest request) {
        return biAskService.ask(request);
    }
}

