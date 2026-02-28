package com.deepbi.sql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class SqlSafetyPolicy {
    private static final Set<String> FORBIDDEN_KEYWORDS = Set.of(
            "insert", "update", "delete", "drop", "alter", "create", "truncate", "replace",
            "grant", "revoke", "call", "load", "outfile", "infile"
    );

    private final int maxRows;

    public SqlSafetyPolicy(@Value("${deepbi.sql.max-rows:200}") int maxRows) {
        this.maxRows = maxRows;
    }

    public String validateAndNormalize(String sql) {
        if (sql == null) {
            throw new IllegalArgumentException("SQL is null");
        }
        String normalized = sql.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("SQL is blank");
        }

        String lower = normalized.toLowerCase(Locale.ROOT);

        if (lower.contains(";")) {
            throw new IllegalArgumentException("Multi-statement SQL is not allowed.");
        }

        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (lower.contains(keyword + " ")) {
                throw new IllegalArgumentException("Forbidden SQL keyword: " + keyword);
            }
        }

        if (!lower.startsWith("select")) {
            throw new IllegalArgumentException("Only SELECT is allowed.");
        }

        if (!lower.contains(" limit ")) {
            normalized = normalized + " LIMIT " + maxRows;
        }

        return normalized;
    }
}

