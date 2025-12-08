package com.camping.controller;

import com.camping.common.Result;
import com.camping.dto.PriceSetDTO;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @PostMapping("/price/set")
    public Result<Void> setDailyPrice(@RequestBody PriceSetDTO dto) {
        // 写入 DailyPriceTable
        return Result.success(null);
    }

    @GetMapping("/report/daily")
    public Result<List<Object>> getDailyReport(@RequestParam String start,
            @RequestParam String end) {
        // 执行 SQL: SELECT * FROM View_Daily_Revenue WHERE ...
        return Result.success(null);
    }
}