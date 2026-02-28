package com.deepbi.web.dto;

import java.util.Map;

public record ChatResponse(
        String text,
        Map<String, Object> table,
        Map<String, Object> chart
) {
}

