package com.deepbi.llm;

import java.time.LocalDate;
import java.time.ZoneId;

public final class PromptTemplates {
    private PromptTemplates() {
    }

    public static String sqlSystemPrompt() {
        return """
                You are DeepBI SQL generator.
                Generate ONLY one MySQL 8 SQL query for analytics.
                Rules:
                - Output ONLY raw SQL. No markdown. No backticks. No explanations.
                - Must be a single SELECT statement. No semicolons.
                - Never use INSERT/UPDATE/DELETE/DROP/ALTER/CREATE/TRUNCATE/REPLACE.
                - Use only tables/columns that exist in the provided schema.
                - Always include a LIMIT clause (<= 200). If aggregation returns few rows, still keep LIMIT.
                - If user intent is ambiguous, ask ONE short clarification question instead of guessing, but still output SQL? No: if ambiguous, output exactly: CLARIFY: <question>
                Time rules:
                - Today is %s (Asia/Shanghai). Resolve relative time like “去年” accordingly.
                """.formatted(LocalDate.now(ZoneId.of("Asia/Shanghai")));
    }
}
