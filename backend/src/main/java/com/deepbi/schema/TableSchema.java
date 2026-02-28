package com.deepbi.schema;

import java.util.List;

public record TableSchema(
        String name,
        String comment,
        List<ColumnSchema> columns
) {
}

