package com.deepbi.web;

import com.deepbi.sql.SqlExecutor;
import com.deepbi.web.dto.TableResult;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/sql")
public class SqlController {
    private final SqlExecutor sqlExecutor;

    public SqlController(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }

    @PostMapping("/query")
    public Mono<TableResult> query(@RequestBody String sql) {
        return Mono.fromCallable(() -> sqlExecutor.query(sql))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/query")
    public Mono<TableResult> queryGet(@RequestParam("q") @NotBlank String sql) {
        return Mono.fromCallable(() -> sqlExecutor.query(sql))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
