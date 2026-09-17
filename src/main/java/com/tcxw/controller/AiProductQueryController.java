package com.tcxw.controller;

import com.tcxw.ai.AiProductQueryService;
import com.tcxw.dto.AiProductQueryRequest;
import com.tcxw.dto.AiProductQueryResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/products")
@ConditionalOnProperty(name = "spring.ai.model.chat", havingValue = "openai")
public class AiProductQueryController {

    private final AiProductQueryService service;

    public AiProductQueryController(AiProductQueryService service) {
        this.service = service;
    }

    @PostMapping("/query")
    public AiProductQueryResponse query(@Valid @RequestBody AiProductQueryRequest request) {
        return new AiProductQueryResponse(service.query(request.question()));
    }
}
