package com.deepbi.bi;

import com.deepbi.web.dto.TableResult;

public record BiAskResponse(
        String question,
        String text,
        String sql,
        TableResult table,
        String clarifyQuestion
) {
    public static BiAskResponse success(String question, String text, String sql, TableResult table) {
        return new BiAskResponse(question, text, sql, table, null);
    }

    public static BiAskResponse clarify(String question, String clarifyQuestion) {
        return new BiAskResponse(question, "需要澄清后才能生成 SQL。", null, null, clarifyQuestion);
    }
}

