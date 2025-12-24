package com.camping.controller;

import com.camping.common.Result;
import com.camping.service.ResourceService;
import com.camping.mapper.DailyPriceMapper;
import com.camping.mapper.SiteMapper;
import com.camping.mapper.BookingEquipMapper;
import com.camping.mapper.EquipmentMapper;
import com.camping.mapper.SiteTypeMapper;
import com.camping.entity.DailyPrice;
import com.camping.entity.Equipment;
import com.camping.entity.Site;
import com.camping.entity.SiteType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 资源查询控制器
 */
@RestController
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private DailyPriceMapper dailyPriceMapper;

    @Autowired
    private SiteMapper siteMapper;

    @Autowired
    private BookingEquipMapper bookingEquipMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private SiteTypeMapper siteTypeMapper;

    /**
     * 获取当日房型列表（含当日价格与可用量）
     */
    @GetMapping("/type/list/today")
    public Result<List<Object>> getSiteTypesToday() {
        try {
            List<Object> list = resourceService.getSiteTypesToday();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取当日房型列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有房型列表
     */
    @GetMapping("/type/list")
    public Result<List<Object>> getSiteTypes() {
        try {
            List<Object> types = resourceService.getSiteTypes();
            return Result.success(types);
        } catch (Exception e) {
            return Result.error("获取房型列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取房型详情
     */
    @GetMapping("/type/{typeId}")
    public Result<Object> getSiteTypeDetail(@PathVariable Long typeId) {
        try {
            Object detail = resourceService.getSiteTypeDetail(typeId);
            return Result.success(detail);
        } catch (Exception e) {
            return Result.error("获取房型详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取价格日历
     * 返回指定日期范围内，每天的价格和库存情况
     */
    @GetMapping("/type/calendar")
    public Result<Object> getCalendar(@RequestParam Long typeId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            Map<String, Object> calendar = resourceService.getCalendar(typeId, startDate, endDate);
            return Result.success(calendar);
        } catch (Exception e) {
            return Result.error("获取日历失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定日期范围的日价
     */
    @PostMapping("/type/prices")
    public Result<List<Object>> getDailyPrices(@RequestBody Map<String, Object> data) {
        try {
            Long typeId = Long.valueOf(data.get("typeId").toString());
            @SuppressWarnings("unchecked")
            List<String> dates = (List<String>) data.get("dates");

            SiteType siteType = siteTypeMapper.selectById(typeId);
            if (siteType == null) {
                return Result.error("房型不存在");
            }

            List<Object> result = new ArrayList<>();
            for (String date : dates) {
                DailyPrice dp = dailyPriceMapper.selectByTypeAndDate(typeId, date);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("specificDate", date);
                item.put("typeId", typeId);
                item.put("price", dp != null ? dp.getPrice() : siteType.getBasePrice());
                item.put("remark", dp != null ? dp.getRemark() : null);
                result.add(item);
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取日价失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有装备列表
     */
    @GetMapping("/equip/list")
    public Result<List<Object>> getEquipments() {
        try {
            List<Object> list = resourceService.getEquipments();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取装备列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取当日装备列表（含当日可用库存）
     */
    @GetMapping("/equip/list/today")
    public Result<List<Object>> getEquipmentsToday() {
        try {
            List<Object> list = resourceService.getEquipmentsToday();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取装备可用库存失败: " + e.getMessage());
        }
    }

    /**
     * 获取装备详情
     */
    @GetMapping("/equip/{equipId}")
    public Result<Object> getEquipmentDetail(@PathVariable Long equipId) {
        try {
            Object detail = resourceService.getEquipmentDetail(equipId);
            return Result.success(detail);
        } catch (Exception e) {
            return Result.error("获取装备详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取装备库存信息
     */
    @PostMapping("/equip/stock")
    public Result<List<Object>> getEquipmentStock(@RequestBody Map<String, Object> data) {
        try {
            @SuppressWarnings("unchecked")
            List<Number> equipIds = (List<Number>) data.get("equipIds");
            String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

            List<Object> result = new ArrayList<>();
            for (Number id : equipIds) {
                Long equipId = id.longValue();
                Equipment e = equipmentMapper.selectById(equipId);
                if (e != null) {
                    Integer usedCount = bookingEquipMapper.sumQuantityByEquipAndDate(equipId, today, today);
                    int used = usedCount != null ? usedCount : 0;
                    int available = (e.getTotalStock() != null ? e.getTotalStock() : 0) - used;

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("equipId", e.getEquipId());
                    item.put("equipName", e.getEquipName());
                    item.put("totalStock", e.getTotalStock());
                    item.put("availableStock", Math.max(available, 0));
                    result.add(item);
                }
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取装备库存失败: " + e.getMessage());
        }
    }

    /**
     * 获取装备分类列表
     */
    @GetMapping("/equip/categories")
    public Result<List<Object>> getEquipmentCategories() {
        try {
            List<Equipment> equipments = equipmentMapper.selectAll();
            Set<String> categories = new LinkedHashSet<>();
            for (Equipment e : equipments) {
                if (e.getCategory() != null && !e.getCategory().isEmpty()) {
                    categories.add(e.getCategory());
                }
            }
            List<Object> result = new ArrayList<>(categories);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取分类列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据分类获取装备列表
     */
    @GetMapping("/equip/category/{category}")
    public Result<List<Object>> getEquipmentsByCategory(@PathVariable String category) {
        try {
            List<Equipment> equipments = equipmentMapper.selectByCategory(category);
            String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

            List<Object> result = new ArrayList<>();
            for (Equipment e : equipments) {
                Integer usedCount = bookingEquipMapper.sumQuantityByEquipAndDate(e.getEquipId(), today, today);
                int used = usedCount != null ? usedCount : 0;
                int available = (e.getTotalStock() != null ? e.getTotalStock() : 0) - used;

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("equipId", e.getEquipId());
                item.put("equipName", e.getEquipName());
                item.put("unitPrice", e.getUnitPrice());
                item.put("totalStock", e.getTotalStock());
                item.put("availableStock", Math.max(available, 0));
                item.put("category", e.getCategory());
                item.put("description", e.getDescription());
                result.add(item);
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取分类装备失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定资源在日期范围内的剩余数量
     * kind: site|equip, typeId: 房型ID或装备类型ID
     */
    @GetMapping(value = "/availability/query", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<Object> queryAvailability(@RequestParam String kind,
            @RequestParam Long typeId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            var data = new java.util.LinkedHashMap<String, Object>();
            data.put("kind", kind);
            data.put("typeId", typeId);
            data.put("startDate", startDate);
            data.put("endDate", endDate);

            if ("site".equals(kind)) {
                // 查询房型在日期范围内的最小可用营位数
                SiteType siteType = siteTypeMapper.selectById(typeId);
                if (siteType == null) {
                    data.put("remaining", 0);
                    data.put("total", 0);
                    return Result.success(data);
                }

                List<Site> allSites = siteMapper.selectByTypeId(typeId);
                int totalSites = allSites.size();

                // 遍历日期范围，取最小可用数
                int minAvailable = totalSites;
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                    String dateStr = date.format(DateTimeFormatter.ISO_DATE);
                    List<Site> availableSites = siteMapper.selectAvailable(typeId, dateStr, dateStr);
                    int available = availableSites != null ? availableSites.size() : totalSites;
                    minAvailable = Math.min(minAvailable, available);
                }

                data.put("remaining", minAvailable);
                data.put("total", totalSites);
            } else if ("equip".equals(kind)) {
                // 查询装备在日期范围内的最小可用库存
                Equipment equipment = equipmentMapper.selectById(typeId);
                if (equipment == null) {
                    data.put("remaining", 0);
                    data.put("total", 0);
                    return Result.success(data);
                }

                int totalStock = equipment.getTotalStock() != null ? equipment.getTotalStock() : 0;

                // 查询日期范围内最大已使用数
                Integer maxUsed = bookingEquipMapper.sumQuantityByEquipAndDate(typeId, startDate, endDate);
                int used = maxUsed != null ? maxUsed : 0;
                int available = totalStock - used;

                data.put("remaining", Math.max(available, 0));
                data.put("total", totalStock);
            } else {
                data.put("remaining", 0);
                data.put("total", 0);
            }

            return Result.success(data);
        } catch (Exception e) {
            return Result.error("查询可用量失败: " + e.getMessage());
        }
    }
}