package com.deepbi.bi;

import com.deepbi.sql.SqlExecutor;
import com.deepbi.sql.SqlSafetyPolicy;
import com.deepbi.sql.TextToSqlResult;
import com.deepbi.sql.TextToSqlService;
import com.deepbi.web.dto.TableResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class BiAskService {
    private final TextToSqlService textToSqlService;
    private final SqlSafetyPolicy sqlSafetyPolicy;
    private final SqlExecutor sqlExecutor;

    public BiAskService(TextToSqlService textToSqlService, SqlSafetyPolicy sqlSafetyPolicy, SqlExecutor sqlExecutor) {
        this.textToSqlService = textToSqlService;
        this.sqlSafetyPolicy = sqlSafetyPolicy;
        this.sqlExecutor = sqlExecutor;
    }

    public Mono<BiAskResponse> ask(BiAskRequest request) {
        String question = request.question() == null ? "" : request.question().trim();
        if (question.isBlank()) {
            return Mono.error(new IllegalArgumentException("question is required"));
        }

        return textToSqlService.generateSql(question)
                .flatMap(result -> handleSqlResult(question, result));
    }

    private Mono<BiAskResponse> handleSqlResult(String question, TextToSqlResult result) {
        if (result.needsClarification()) {
            return Mono.just(BiAskResponse.clarify(question, result.clarifyQuestion()));
        }

        String rawSql = result.sql();
        if (rawSql == null || rawSql.isBlank()) {
            return Mono.error(new IllegalStateException("LLM returned empty SQL"));
        }

        String safeSql = sqlSafetyPolicy.validateAndNormalize(rawSql);

        return Mono.fromCallable(() -> sqlExecutor.query(safeSql))
                .subscribeOn(Schedulers.boundedElastic())
                .map(table -> success(question, safeSql, table));
    }

    private static BiAskResponse success(String question, String sql, TableResult table) {
        String summary = "已生成 SQL 并执行完成，返回 " + (table == null ? 0 : table.rows().size()) + " 行。";
        return BiAskResponse.success(question, summary, sql, table);
    }
}

