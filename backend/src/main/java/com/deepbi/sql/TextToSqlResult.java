package com.deepbi.sql;

public record TextToSqlResult(
        String sql,
        String clarifyQuestion
) {
    public static TextToSqlResult sql(String sql) {
        return new TextToSqlResult(sql, null);
    }

    public static TextToSqlResult clarify(String question) {
        return new TextToSqlResult(null, question);
    }

    public boolean needsClarification() {
        return clarifyQuestion != null && !clarifyQuestion.isBlank();
    }
}

