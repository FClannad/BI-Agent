package com.deepbi.schema;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class SchemaService {
    private final JdbcTemplate jdbcTemplate;

    private volatile CachedSchema cachedSchema = null;

    public SchemaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TableSchema> searchRelevantTables(String question, int maxTables) {
        List<TableSchema> all = loadAllTables();
        if (all.isEmpty()) return all;

        Set<String> tokens = tokenize(question);
        if (tokens.isEmpty()) {
            return all.subList(0, Math.min(maxTables, all.size()));
        }

        record Scored(TableSchema table, int score) {
        }

        List<Scored> scored = new ArrayList<>(all.size());
        for (TableSchema table : all) {
            int score = score(table, tokens);
            if (score > 0) {
                scored.add(new Scored(table, score));
            }
        }

        scored.sort((a, b) -> Integer.compare(b.score(), a.score()));

        if (scored.isEmpty()) {
            return all.subList(0, Math.min(maxTables, all.size()));
        }

        List<TableSchema> result = new ArrayList<>();
        for (int i = 0; i < Math.min(maxTables, scored.size()); i++) {
            result.add(scored.get(i).table());
        }
        return result;
    }

    public List<TableSchema> loadAllTables() {
        CachedSchema current = cachedSchema;
        if (current != null && !current.isExpired()) {
            return current.tables();
        }

        List<TableSchema> tables = fetchSchemaFromInformationSchema();
        cachedSchema = new CachedSchema(tables, Instant.now());
        return tables;
    }

    private List<TableSchema> fetchSchemaFromInformationSchema() {
        String dbName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (dbName == null || dbName.isBlank()) {
            return List.of();
        }

        List<Map<String, Object>> tableRows = jdbcTemplate.queryForList("""
                SELECT table_name AS tableName, table_comment AS tableComment
                FROM information_schema.tables
                WHERE table_schema = ?
                ORDER BY table_name
                """, dbName);

        Map<String, String> tableComments = new LinkedHashMap<>();
        for (Map<String, Object> row : tableRows) {
            String tableName = Objects.toString(row.get("tableName"), "");
            String tableComment = Objects.toString(row.get("tableComment"), "");
            if (!tableName.isBlank()) {
                tableComments.put(tableName, tableComment);
            }
        }

        if (tableComments.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> columnRows = jdbcTemplate.queryForList("""
                SELECT table_name AS tableName,
                       column_name AS columnName,
                       column_type AS columnType,
                       column_comment AS columnComment
                FROM information_schema.columns
                WHERE table_schema = ?
                ORDER BY table_name, ordinal_position
                """, dbName);

        Map<String, List<ColumnSchema>> columnsByTable = new LinkedHashMap<>();
        for (Map<String, Object> row : columnRows) {
            String tableName = Objects.toString(row.get("tableName"), "");
            if (tableName.isBlank()) continue;

            columnsByTable.computeIfAbsent(tableName, k -> new ArrayList<>())
                    .add(new ColumnSchema(
                            Objects.toString(row.get("columnName"), ""),
                            Objects.toString(row.get("columnType"), ""),
                            Objects.toString(row.get("columnComment"), "")
                    ));
        }

        List<TableSchema> schemas = new ArrayList<>(tableComments.size());
        for (Map.Entry<String, String> entry : tableComments.entrySet()) {
            String tableName = entry.getKey();
            String tableComment = entry.getValue();
            List<ColumnSchema> cols = columnsByTable.getOrDefault(tableName, List.of());
            schemas.add(new TableSchema(tableName, tableComment, cols));
        }

        return schemas;
    }

    private static int score(TableSchema table, Set<String> tokens) {
        int score = 0;
        String tName = table.name() == null ? "" : table.name().toLowerCase(Locale.ROOT);
        String tComment = table.comment() == null ? "" : table.comment().toLowerCase(Locale.ROOT);

        for (String token : tokens) {
            if (tName.contains(token)) score += 5;
            if (!tComment.isBlank() && tComment.contains(token)) score += 3;
        }

        for (ColumnSchema col : table.columns()) {
            String cName = col.name() == null ? "" : col.name().toLowerCase(Locale.ROOT);
            String cComment = col.comment() == null ? "" : col.comment().toLowerCase(Locale.ROOT);
            for (String token : tokens) {
                if (cName.contains(token)) score += 2;
                if (!cComment.isBlank() && cComment.contains(token)) score += 1;
            }
        }
        return score;
    }

    private static Set<String> tokenize(String text) {
        if (text == null) return Set.of();
        String lowered = text.toLowerCase(Locale.ROOT);

        Set<String> tokens = new LinkedHashSet<>();
        for (String part : lowered.split("[^a-z0-9\\u4e00-\\u9fa5]+")) {
            String p = part.trim();
            if (p.length() >= 2) tokens.add(p);
        }
        return tokens;
    }

    private record CachedSchema(List<TableSchema> tables, Instant loadedAt) {
        boolean isExpired() {
            return loadedAt.plus(Duration.ofMinutes(10)).isBefore(Instant.now());
        }
    }
}

