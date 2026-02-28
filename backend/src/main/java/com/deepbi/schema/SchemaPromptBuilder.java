package com.deepbi.schema;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SchemaPromptBuilder {
    public String buildSchemaContext(List<TableSchema> tables) {
        StringBuilder sb = new StringBuilder();
        sb.append("### Database schema (MySQL)\n");
        for (TableSchema table : tables) {
            sb.append("- table: ").append(table.name());
            if (table.comment() != null && !table.comment().isBlank()) {
                sb.append("  # ").append(table.comment());
            }
            sb.append("\n");
            for (ColumnSchema col : table.columns()) {
                sb.append("  - ").append(col.name())
                        .append(" ").append(col.type());
                if (col.comment() != null && !col.comment().isBlank()) {
                    sb.append("  # ").append(col.comment());
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}

