package com.camping.controller;

import com.camping.common.Result;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class ResourceController {

    @GetMapping("/type/list")
    public Result<List<Object>> getSiteTypes() {
        // 返回 List<SiteTypeItem>
        return Result.success(null);
    }

    @GetMapping("/type/calendar")
    public Result<List<Object>> getCalendar(@RequestParam Long typeId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        // 返回 List<DailyPrice>
        return Result.success(null);
    }

    @GetMapping("/equip/list")
    public Result<List<Object>> getEquipments() {
        // 返回 List<EquipItem>
        return Result.success(null);
    }
}