package com.camping.service.impl;

import com.camping.entity.SiteType;
import com.camping.entity.Site;
import com.camping.entity.Equipment;
import com.camping.entity.DailyPrice;
import com.camping.mapper.SiteTypeMapper;
import com.camping.mapper.EquipmentMapper;
import com.camping.mapper.SiteMapper;
import com.camping.mapper.DailyPriceMapper;
import com.camping.mapper.BookingMapper;
import com.camping.mapper.BookingEquipMapper;
import com.camping.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 资源业务实现
 */
@Service
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private SiteTypeMapper siteTypeMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private SiteMapper siteMapper;

    @Autowired
    private DailyPriceMapper dailyPriceMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private BookingEquipMapper bookingEquipMapper;

    /**
     * 获取所有房型列表
     */
    @Override
    public List<Object> getSiteTypes() throws Exception {
        List<SiteType> types = siteTypeMapper.selectAll();
        List<Object> result = new ArrayList<>();
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        for (SiteType t : types) {
            List<Site> sites = siteMapper.selectByTypeId(t.getTypeId());
            int totalSites = sites.size();
            // 查询今天可用营位数
            List<Site> availableSites = siteMapper.selectAvailable(t.getTypeId(), today, today);
            int available = availableSites != null ? availableSites.size() : totalSites;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("typeId", t.getTypeId());
            item.put("typeName", t.getTypeName());
            item.put("basePrice", t.getBasePrice());
            item.put("maxGuests", t.getMaxGuests());
            item.put("totalSites", totalSites);
            item.put("availableSites", available);
            item.put("description", t.getDescription());
            item.put("imageUrl", t.getImageUrl());
            result.add(item);
        }
        return result;
    }

    /**
     * 获取当日房型列表（含当日价格与可用量）
     */
    @Override
    public List<Object> getSiteTypesToday() throws Exception {
        List<SiteType> types = siteTypeMapper.selectAll();
        List<Object> result = new ArrayList<>();
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        for (SiteType t : types) {
            // 查询当日浮动价格
            DailyPrice dp = dailyPriceMapper.selectByTypeAndDate(t.getTypeId(), today);
            BigDecimal priceToday = (dp != null && dp.getPrice() != null) ? dp.getPrice() : t.getBasePrice();

            // 查询营位总数和可用数
            List<Site> sites = siteMapper.selectByTypeId(t.getTypeId());
            int totalSites = sites.size();
            List<Site> availableSites = siteMapper.selectAvailable(t.getTypeId(), today, today);
            int available = availableSites != null ? availableSites.size() : totalSites;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("typeId", t.getTypeId());
            item.put("typeName", t.getTypeName());
            item.put("priceToday", priceToday);
            item.put("basePrice", t.getBasePrice());
            item.put("maxGuests", t.getMaxGuests());
            item.put("totalSites", totalSites);
            item.put("availableSites", available);
            item.put("description", t.getDescription());
            item.put("imageUrl", t.getImageUrl());
            result.add(item);
        }
        return result;
    }

    /**
     * 获取房型详情
     */
    @Override
    public Object getSiteTypeDetail(Long typeId) throws Exception {
        if (typeId == null) {
            throw new Exception("房型ID不能为空");
        }

        SiteType t = siteTypeMapper.selectById(typeId);
        if (t == null) {
            throw new Exception("房型不存在");
        }

        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        List<Site> sites = siteMapper.selectByTypeId(typeId);
        int totalSites = sites.size();
        List<Site> availableSites = siteMapper.selectAvailable(typeId, today, today);
        int available = availableSites != null ? availableSites.size() : totalSites;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("typeId", t.getTypeId());
        result.put("typeName", t.getTypeName());
        result.put("basePrice", t.getBasePrice());
        result.put("maxGuests", t.getMaxGuests());
        result.put("totalSites", totalSites);
        result.put("availableSites", available);
        result.put("description", t.getDescription());
        result.put("imageUrl", t.getImageUrl());
        result.put("status", t.getStatus());
        return result;
    }

    /**
     * 获取价格日历
     * 需要组合多个表的信息：房型、营位、预订、日价格
     */
    @Override
    public Map<String, Object> getCalendar(Long typeId, String startDate, String endDate) throws Exception {
        if (typeId == null || startDate == null || endDate == null) {
            throw new Exception("参数不完整");
        }

        Map<String, Object> calendar = new HashMap<>();

        SiteType siteType = siteTypeMapper.selectById(typeId);
        if (siteType == null) {
            throw new Exception("房型不存在");
        }

        List<Site> allSites = siteMapper.selectByTypeId(typeId);
        int totalSites = allSites.size();

        // 查询日期范围内的浮动价格
        List<DailyPrice> dailyPrices = dailyPriceMapper.selectByTypeAndDateRange(typeId, startDate, endDate);
        Map<String, BigDecimal> priceMap = new HashMap<>();
        for (DailyPrice dp : dailyPrices) {
            priceMap.put(dp.getSpecificDate(), dp.getPrice());
        }

        // 遍历日期范围生成日历数据
        List<Map<String, Object>> calendarData = new ArrayList<>();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String dateStr = date.format(fmt);
            BigDecimal price = priceMap.getOrDefault(dateStr, siteType.getBasePrice());
            List<Site> availableSites = siteMapper.selectAvailable(typeId, dateStr, dateStr);
            int available = availableSites != null ? availableSites.size() : totalSites;

            Map<String, Object> dayData = new LinkedHashMap<>();
            dayData.put("date", dateStr);
            dayData.put("price", price);
            dayData.put("available", available > 0);
            dayData.put("stock", available);
            calendarData.add(dayData);
        }

        calendar.put("typeId", typeId);
        calendar.put("typeName", siteType.getTypeName());
        calendar.put("basePrice", siteType.getBasePrice());
        calendar.put("calendarData", calendarData);

        return calendar;
    }

    /**
     * 获取装备列表
     */
    @Override
    public List<Object> getEquipments() throws Exception {
        List<Equipment> equipments = equipmentMapper.selectAll();
        List<Object> result = new ArrayList<>();
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        for (Equipment e : equipments) {
            // 计算当日已预订数量
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
        return result;
    }

    /**
     * 获取当日装备列表（含当日可用库存）
     */
    @Override
    public List<Object> getEquipmentsToday() throws Exception {
        return getEquipments(); // 逻辑相同，都基于当日计算
    }

    /**
     * 获取装备详情
     */
    @Override
    public Object getEquipmentDetail(Long equipId) throws Exception {
        if (equipId == null) {
            throw new Exception("装备ID不能为空");
        }

        Equipment e = equipmentMapper.selectById(equipId);
        if (e == null) {
            throw new Exception("装备不存在");
        }

        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        Integer usedCount = bookingEquipMapper.sumQuantityByEquipAndDate(equipId, today, today);
        int used = usedCount != null ? usedCount : 0;
        int available = (e.getTotalStock() != null ? e.getTotalStock() : 0) - used;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("equipId", e.getEquipId());
        result.put("equipName", e.getEquipName());
        result.put("unitPrice", e.getUnitPrice());
        result.put("totalStock", e.getTotalStock());
        result.put("availableStock", Math.max(available, 0));
        result.put("category", e.getCategory());
        result.put("description", e.getDescription());
        result.put("status", e.getStatus());
        return result;
    }
}
