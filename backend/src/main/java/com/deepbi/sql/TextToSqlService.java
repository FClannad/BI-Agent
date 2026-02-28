package com.deepbi.sql;

import com.deepbi.llm.LlmClient;
import com.deepbi.llm.PromptTemplates;
import com.deepbi.schema.SchemaPromptBuilder;
import com.deepbi.schema.SchemaService;
import com.deepbi.schema.TableSchema;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
public class TextToSqlService {
    private final LlmClient llmClient;
    private final SchemaService schemaService;
    private final SchemaPromptBuilder schemaPromptBuilder;

    public TextToSqlService(LlmClient llmClient, SchemaService schemaService, SchemaPromptBuilder schemaPromptBuilder) {
        this.llmClient = llmClient;
        this.schemaService = schemaService;
        this.schemaPromptBuilder = schemaPromptBuilder;
    }

    public Mono<TextToSqlResult> generateSql(String question) {
        return Mono.fromCallable(() -> schemaService.searchRelevantTables(question, 5))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(tables -> {
                    if (tables == null || tables.isEmpty()) {
                        return Mono.just(TextToSqlResult.clarify("数据库 Schema 为空或无法读取。请检查 MySQL 连接/权限，并确保当前库存在业务表。"));
                    }

                    String schemaContext = schemaPromptBuilder.buildSchemaContext(tables);
                    String userPrompt = """
                            %s

                            ### User question
                            %s
                            """.formatted(schemaContext, question);

                    return llmClient.chat(PromptTemplates.sqlSystemPrompt(), userPrompt)
                            .map(TextToSqlService::normalizeModelOutput)
                            .map(sqlOrClarify -> {
                                if (sqlOrClarify.startsWith("CLARIFY:")) {
                                    return TextToSqlResult.clarify(sqlOrClarify.substring("CLARIFY:".length()).trim());
                                }
                                return TextToSqlResult.sql(sqlOrClarify);
                            });
                });
    }

    private static String normalizeModelOutput(String text) {
        if (text == null) return "";
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replace("```sql", "").replace("```", "").trim();
        }
        trimmed = trimmed.replace("\r\n", "\n").trim();
        if (trimmed.endsWith(";")) trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        return trimmed;
    }
}
