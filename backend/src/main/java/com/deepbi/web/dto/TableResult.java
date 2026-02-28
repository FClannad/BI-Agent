package com.deepbi.web.dto;

import java.util.List;
import java.util.Map;

public record TableResult(
        List<Column> columns,
        List<Map<String, Object>> rows
) {
    public record Column(
            String name,
            String type
    ) {
    }
}

