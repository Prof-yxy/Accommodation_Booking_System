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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 资源业务实现
 */
@Service
public class ResourceServiceImpl implements ResourceService {

    private static final BigDecimal WEEKDAY_RATE = BigDecimal.ONE;
    private static final BigDecimal WEEKEND_RATE = new BigDecimal("1.15");

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
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;
        String today = LocalDate.now().format(fmt);
        String tomorrow = LocalDate.now().plusDays(1).format(fmt);

        for (SiteType t : types) {
            List<Site> sites = siteMapper.selectByTypeId(t.getTypeId());
            int totalSites = sites.size();
            // 查询今天可用营位数
            List<Site> availableSites = siteMapper.selectAvailable(t.getTypeId(), today, tomorrow);
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
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;
        String today = LocalDate.now().format(fmt);
        String tomorrow = LocalDate.now().plusDays(1).format(fmt);

        for (SiteType t : types) {
            // 查询当日价格（含周末加成/特价）
            BigDecimal priceToday = resolveDailyPrice(t, today);

            // 查询营位总数和可用数
            List<Site> sites = siteMapper.selectByTypeId(t.getTypeId());
            int totalSites = sites.size();
            List<Site> availableSites = siteMapper.selectAvailable(t.getTypeId(), today, tomorrow);
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

        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;
        String today = LocalDate.now().format(fmt);
        String tomorrow = LocalDate.now().plusDays(1).format(fmt);
        List<Site> sites = siteMapper.selectByTypeId(typeId);
        int totalSites = sites.size();
        List<Site> availableSites = siteMapper.selectAvailable(typeId, today, tomorrow);
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
            BigDecimal price = priceMap.getOrDefault(dateStr, resolveDailyPrice(siteType, dateStr));
            String nextDay = date.plusDays(1).format(fmt);
            List<Site> availableSites = siteMapper.selectAvailable(typeId, dateStr, nextDay);
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
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;
        String today = LocalDate.now().format(fmt);
        String tomorrow = LocalDate.now().plusDays(1).format(fmt);

        for (Equipment e : equipments) {
            // 计算当日已预订数量
            Integer usedCount = bookingEquipMapper.sumQuantityByEquipAndDate(e.getEquipId(), today, tomorrow);
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

    private boolean isWeekend(LocalDate date) {
        int day = date.getDayOfWeek().getValue();
        return day == 6 || day == 7;
    }

    private BigDecimal resolveDailyPrice(SiteType type, String dateStr) {
        DailyPrice dp = dailyPriceMapper.selectByTypeAndDate(type.getTypeId(), dateStr);
        if (dp != null && dp.getPrice() != null) {
            return dp.getPrice();
        }
        LocalDate date = LocalDate.parse(dateStr);
        BigDecimal factor = isWeekend(date) ? WEEKEND_RATE : WEEKDAY_RATE;
        return type.getBasePrice().multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
