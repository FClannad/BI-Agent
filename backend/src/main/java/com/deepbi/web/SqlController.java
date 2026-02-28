package com.deepbi.web;

import com.deepbi.sql.SqlExecutor;
import com.deepbi.web.dto.TableResult;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sql")
public class SqlController {
    private final SqlExecutor sqlExecutor;

    public SqlController(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }

    @PostMapping("/query")
    public TableResult query(@RequestBody String sql) {
        return sqlExecutor.query(sql);
    }

    @GetMapping("/query")
    public TableResult queryGet(@RequestParam("q") @NotBlank String sql) {
        return sqlExecutor.query(sql);
    }
}

