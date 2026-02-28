package com.deepbi.sql;

import com.deepbi.web.dto.TableResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSetMetaData;
import java.util.*;

@Component
public class SqlExecutor {
    private final JdbcTemplate jdbcTemplate;
    private final SqlSafetyPolicy safetyPolicy;

    public SqlExecutor(JdbcTemplate jdbcTemplate, SqlSafetyPolicy safetyPolicy) {
        this.jdbcTemplate = jdbcTemplate;
        this.safetyPolicy = safetyPolicy;
    }

    public TableResult query(String sql) {
        String safeSql = safetyPolicy.validateAndNormalize(sql);

        return jdbcTemplate.query(safeSql, rs -> {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            List<TableResult.Column> columns = new ArrayList<>(colCount);
            for (int i = 1; i <= colCount; i++) {
                columns.add(new TableResult.Column(meta.getColumnLabel(i), meta.getColumnTypeName(i)));
            }

            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                rows.add(row);
            }

            return new TableResult(columns, rows);
        });
    }
}

