package com.camping.service.impl;

import com.camping.dto.BookingCheckDTO;
import com.camping.dto.BookingCreateDTO;
import com.camping.dto.EquipSelectDTO;
import com.camping.entity.*;
import com.camping.mapper.*;
import com.camping.service.BookingService;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 预订业务实现
 */
@Service
public class BookingServiceImpl implements BookingService {

    private static final BigDecimal WEEKDAY_RATE = BigDecimal.ONE;
    private static final BigDecimal WEEKEND_RATE = new BigDecimal("1.15");
    private static final BigDecimal DAYPASS_RATE = new BigDecimal("0.60");

    @Autowired
    private SiteTypeMapper siteTypeMapper;

    @Autowired
    private SiteMapper siteMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private DailyPriceMapper dailyPriceMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private BookingEquipMapper bookingEquipMapper;

    /**
     * 预订前检查 - 不锁库存，仅计算价格
     */
    @Override
    public Map<String, Object> checkBooking(BookingCheckDTO dto) throws Exception {
        if (dto.getTypeId() == null || dto.getCheckIn() == null || dto.getCheckOut() == null) {
            throw new Exception("参数不完整");
        }

        Map<String, Object> result = new LinkedHashMap<>();

        try {
            // 1. 查询房型信息
            SiteType siteType = siteTypeMapper.selectById(dto.getTypeId());

            if (siteType == null) {
                result.put("isAvailable", false);
                result.put("msg", "当前仅支持先选择房型，再附加装备下单（不支持仅装备预订）");
                return result;
            }

            boolean isDayPass = isDayPass(dto.getCheckIn(), dto.getCheckOut());
            LocalDate startDate = LocalDate.parse(dto.getCheckIn());
            LocalDate endExclusive = resolveEffectiveCheckout(dto.getCheckIn(), dto.getCheckOut());
            int nights = Math.max(1, (int) ChronoUnit.DAYS.between(startDate, endExclusive));

            // 3. 查询日价格并累计
            List<Map<String, Object>> priceDetail = new ArrayList<>();
            List<String> siteTerms = new ArrayList<>();
            BigDecimal sitePriceBeforeMode = BigDecimal.ZERO;

            for (LocalDate date = startDate; date.isBefore(endExclusive); date = date.plusDays(1)) {
                DayPriceInfo dpi = resolveDayPriceInfo(siteType, dto.getTypeId(), date);
                sitePriceBeforeMode = sitePriceBeforeMode.add(dpi.price);

                Map<String, Object> dayDetail = new LinkedHashMap<>();
                dayDetail.put("date", dpi.date);
                dayDetail.put("price", dpi.price);
                dayDetail.put("weekend", dpi.weekend);
                dayDetail.put("specialPrice", dpi.specialPrice);
                priceDetail.add(dayDetail);

                String term = dpi.date + ":" + dpi.price
                        + (dpi.specialPrice ? "(特价)" : (dpi.weekend ? "(周末1.15x基础)" : "(工作日1.0x基础)"));
                siteTerms.add(term);
            }

            BigDecimal sitePrice = sitePriceBeforeMode;
            if (isDayPass) {
                sitePrice = sitePrice.multiply(DAYPASS_RATE).setScale(2, RoundingMode.HALF_UP);
            }

            // 4. 检查营位可用性
            List<Site> availableSites = siteMapper.selectAvailable(dto.getTypeId(), dto.getCheckIn(),
                    endExclusive.format(DateTimeFormatter.ISO_DATE));
            boolean siteAvailable = availableSites != null && !availableSites.isEmpty();

            // 5. 计算装备价格并检查库存
            BigDecimal equipmentPrice = BigDecimal.ZERO;
            List<String> equipTerms = new ArrayList<>();
            boolean equipAvailable = true;

            if (dto.getEquipments() != null && !dto.getEquipments().isEmpty()) {
                for (EquipSelectDTO equip : dto.getEquipments()) {
                    Equipment equipment = equipmentMapper.selectById(equip.getEquipId());
                    if (equipment != null) {
                        // 计算装备价格 (按天计费)
                        BigDecimal equipCost = equipment.getUnitPrice()
                                .multiply(new BigDecimal(equip.getCount()))
                                .multiply(new BigDecimal(nights));
                        equipmentPrice = equipmentPrice.add(equipCost);
                        equipTerms.add(String.format("%s %.2f x %d x %d = %.2f", equipment.getEquipName(),
                                equipment.getUnitPrice(), equip.getCount(), nights, equipCost));

                        // 检查库存
                        Integer usedCount = bookingEquipMapper.sumQuantityByEquipAndDate(
                                equip.getEquipId(), dto.getCheckIn(), endExclusive.format(DateTimeFormatter.ISO_DATE));
                        int used = usedCount != null ? usedCount : 0;
                        int available = (equipment.getTotalStock() != null ? equipment.getTotalStock() : 0) - used;

                        if (available < equip.getCount()) {
                            equipAvailable = false;
                        }
                    }
                }
            }

            // 6. 计算总价
            BigDecimal totalPrice = sitePrice.add(equipmentPrice);

            boolean isAvailable = siteAvailable && equipAvailable;
            String msg = isAvailable ? "可预订" : (!siteAvailable ? "营位已满" : "装备库存不足");

            result.put("isAvailable", isAvailable);
            result.put("msg", msg);
            result.put("totalPrice", totalPrice);

            Map<String, Object> priceDetailMap = new LinkedHashMap<>();
            priceDetailMap.put("sitePrice", sitePrice);
            priceDetailMap.put("dailyPrices", priceDetail);
            priceDetailMap.put("equipmentPrice", equipmentPrice);
            priceDetailMap.put("nights", nights);
            priceDetailMap.put("mode", isDayPass ? "daypass" : "overnight");

            StringBuilder formula = new StringBuilder();
            formula.append("房型: ").append(String.join(" + ", siteTerms))
                    .append(" = ").append(sitePriceBeforeMode.setScale(2, RoundingMode.HALF_UP));
            if (isDayPass) {
                formula.append(" × 日营系数0.60 = ").append(sitePrice);
            }
            if (!equipTerms.isEmpty()) {
                formula.append("；装备: ").append(String.join(" + ", equipTerms))
                        .append(" = ").append(equipmentPrice.setScale(2, RoundingMode.HALF_UP));
            }
            formula.append("；总价 = 房型(").append(sitePrice.setScale(2, RoundingMode.HALF_UP))
                    .append(") + 装备(").append(equipmentPrice.setScale(2, RoundingMode.HALF_UP))
                    .append(") = ").append(totalPrice.setScale(2, RoundingMode.HALF_UP));
            priceDetailMap.put("formula", formula.toString());

            result.put("priceDetail", priceDetailMap);

            return result;

        } catch (Exception e) {
            result.put("isAvailable", false);
            result.put("msg", "检查失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 创建订单 - 核心事务方法
     * 对应文档: @Transactional 注解
     * 1. 校验库存
     * 2. 计算价格
     * 3. 分配营位
     * 4. 保存订单和装备关联
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(BookingCreateDTO dto) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            // 1. 参数验证
            if (dto.getUserId() == null || dto.getTypeId() == null) {
                throw new Exception("用户ID或房型ID不能为空");
            }
            int quantity = dto.getQuantity() != null && dto.getQuantity() > 0 ? dto.getQuantity() : 1;

            // 2. 查询房型
            SiteType siteType = siteTypeMapper.selectById(dto.getTypeId());
            if (siteType == null) {
                throw new Exception("当前仅支持先选择房型，再附加装备下单（不支持仅装备预订）");
            }

            boolean isDayPass = isDayPass(dto.getCheckIn(), dto.getCheckOut());
            LocalDate startDate = LocalDate.parse(dto.getCheckIn());
            LocalDate endExclusive = resolveEffectiveCheckout(dto.getCheckIn(), dto.getCheckOut());
            int nights = Math.max(1, (int) ChronoUnit.DAYS.between(startDate, endExclusive));

            // 3. 装备库存检查
            if (dto.getEquipments() != null && !dto.getEquipments().isEmpty()) {
                for (EquipSelectDTO equip : dto.getEquipments()) {
                    Equipment equipment = equipmentMapper.selectById(equip.getEquipId());
                    if (equipment == null) {
                        throw new Exception("装备不存在: " + equip.getEquipId());
                    }

                    Integer usedCount = bookingEquipMapper.sumQuantityByEquipAndDate(
                            equip.getEquipId(), dto.getCheckIn(), endExclusive.format(DateTimeFormatter.ISO_DATE));
                    int used = usedCount != null ? usedCount : 0;
                    int available = (equipment.getTotalStock() != null ? equipment.getTotalStock() : 0) - used;

                    if (available < equip.getCount()) {
                        throw new Exception("装备库存不足: " + equipment.getEquipName());
                    }
                }
            }

            // 4. 营位自动分配
            List<Site> availableSites = siteMapper.selectAvailable(dto.getTypeId(), dto.getCheckIn(),
                    endExclusive.format(DateTimeFormatter.ISO_DATE));
            if (availableSites == null || availableSites.size() < quantity) {
                throw new Exception("可用营位不足, 剩余: " + (availableSites == null ? 0 : availableSites.size()));
            }

            // 5. 价格计算 (服务端计算，不信任前端传来的价格)
            BigDecimal sitePriceBeforeMode = BigDecimal.ZERO;
            List<String> siteTerms = new ArrayList<>();
            for (LocalDate date = startDate; date.isBefore(endExclusive); date = date.plusDays(1)) {
                DayPriceInfo dpi = resolveDayPriceInfo(siteType, dto.getTypeId(), date);
                sitePriceBeforeMode = sitePriceBeforeMode.add(dpi.price);
                String term = dpi.date + ":" + dpi.price
                        + (dpi.specialPrice ? "(特价)" : (dpi.weekend ? "(周末1.15x基础)" : "(工作日1.0x基础)"));
                siteTerms.add(term);
            }

            BigDecimal sitePrice = sitePriceBeforeMode;
            if (isDayPass) {
                sitePrice = sitePrice.multiply(DAYPASS_RATE).setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal equipmentPrice = BigDecimal.ZERO;
            List<String> equipTerms = new ArrayList<>();
            if (dto.getEquipments() != null && !dto.getEquipments().isEmpty()) {
                for (EquipSelectDTO equip : dto.getEquipments()) {
                    Equipment equipment = equipmentMapper.selectById(equip.getEquipId());
                    if (equipment != null) {
                        BigDecimal equipCost = equipment.getUnitPrice()
                                .multiply(new BigDecimal(equip.getCount()))
                                .multiply(new BigDecimal(nights));
                        equipmentPrice = equipmentPrice.add(equipCost);
                        equipTerms.add(String.format("%s %.2f x %d x %d = %.2f", equipment.getEquipName(),
                                equipment.getUnitPrice(), equip.getCount(), nights, equipCost));
                    }
                }
            }

            BigDecimal totalPricePerSite = sitePrice;
            BigDecimal totalPrice = totalPricePerSite.multiply(new BigDecimal(quantity)).add(equipmentPrice);

            List<Long> bookingIds = new ArrayList<>();
            List<String> siteNos = new ArrayList<>();

            for (int i = 0; i < quantity; i++) {
                Site allocatedSite = availableSites.get(i);

                Booking booking = new Booking();
                booking.setUserId(dto.getUserId());
                booking.setTypeId(dto.getTypeId());
                booking.setSiteId(allocatedSite.getSiteId());
                booking.setCheckIn(dto.getCheckIn());
                booking.setCheckOut(dto.getCheckOut());
                booking.setGuestName(dto.getGuestName());
                booking.setGuestPhone(dto.getGuestPhone());
                booking.setTotalPrice(totalPricePerSite.add(equipmentPrice));
                booking.setStatus(0); // 0: 待支付
                booking.setCreateTime(LocalDateTime.now());

                bookingMapper.insert(booking);
                Long bookingId = booking.getBookingId();
                bookingIds.add(bookingId);
                siteNos.add(allocatedSite.getSiteNo());

                // 保存装备关联
                if (dto.getEquipments() != null && !dto.getEquipments().isEmpty()) {
                    for (EquipSelectDTO equip : dto.getEquipments()) {
                        BookingEquip bookingEquip = new BookingEquip();
                        bookingEquip.setBookingId(bookingId);
                        bookingEquip.setEquipId(equip.getEquipId());
                        bookingEquip.setQuantity(equip.getCount());

                        bookingEquipMapper.insert(bookingEquip);
                    }
                }
            }

            // 返回结果
            result.put("bookingIds", bookingIds);
            result.put("siteNos", siteNos);
            result.put("totalPrice", totalPrice);
            result.put("status", 0);
            result.put("quantity", quantity);

            Map<String, Object> priceDetailMap = new LinkedHashMap<>();
            priceDetailMap.put("sitePrice", sitePrice);
            priceDetailMap.put("equipmentPrice", equipmentPrice);
            priceDetailMap.put("nights", nights);
            priceDetailMap.put("mode", isDayPass ? "daypass" : "overnight");

            StringBuilder formula = new StringBuilder();
            formula.append("房型: ").append(String.join(" + ", siteTerms))
                    .append(" = ").append(sitePriceBeforeMode.setScale(2, RoundingMode.HALF_UP));
            if (isDayPass) {
                formula.append(" × 日营系数0.60 = ").append(sitePrice);
            }
            if (!equipTerms.isEmpty()) {
                formula.append("；装备: ").append(String.join(" + ", equipTerms))
                        .append(" = ").append(equipmentPrice.setScale(2, RoundingMode.HALF_UP));
            }
            formula.append("；总价 = 房型(").append(sitePrice.setScale(2, RoundingMode.HALF_UP))
                    .append(") + 装备(").append(equipmentPrice.setScale(2, RoundingMode.HALF_UP))
                    .append(") = ").append(totalPrice.setScale(2, RoundingMode.HALF_UP));
            priceDetailMap.put("formula", formula.toString());
            result.put("priceDetail", priceDetailMap);

            return result;

        } catch (Exception e) {
            throw new Exception("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 支付订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payBooking(Long bookingId) throws Exception {
        if (bookingId == null) {
            throw new Exception("订单ID不能为空");
        }

        Booking booking = bookingMapper.selectById(bookingId);

        if (booking == null) {
            throw new Exception("订单不存在");
        }

        if (booking.getStatus() != 0) { // 0: 待支付
            throw new Exception("订单状态无效，无法支付");
        }

        booking.setStatus(1); // 1: 已完成/已支付
        booking.setUpdateTime(LocalDateTime.now());

        bookingMapper.update(booking);
    }

    /**
     * 获取我的订单
     */
    @Override
    public List<Booking> getMyBookings(Long userId, Integer status) throws Exception {
        if (userId == null) {
            throw new Exception("用户ID不能为空");
        }

        if (status != null) {
            return bookingMapper.selectByUserIdAndStatus(userId, status);
        }
        return bookingMapper.selectByUserId(userId);
    }

    /**
     * 获取订单详情
     */
    @Override
    public Booking getBookingDetail(Long bookingId) throws Exception {
        if (bookingId == null) {
            throw new Exception("订单ID不能为空");
        }

        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new Exception("订单不存在");
        }
        return booking;
    }

    @Override
    public List<Booking> getAllBookings() throws Exception {
        return bookingMapper.selectAll();
    }

    /**
     * 取消订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long bookingId) throws Exception {
        finalizeBooking(bookingId, 3);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endBooking(Long bookingId) throws Exception {
        finalizeBooking(bookingId, 2);
    }

    /**
     * 获取订单的装备列表
     */
    @Override
    public List<Object> getBookingEquipments(Long bookingId) throws Exception {
        if (bookingId == null) {
            throw new Exception("订单ID不能为空");
        }

        List<BookingEquip> bookingEquips = bookingEquipMapper.selectByBookingId(bookingId);
        List<Object> result = new ArrayList<>();

        for (BookingEquip be : bookingEquips) {
            Equipment equipment = equipmentMapper.selectById(be.getEquipId());
            if (equipment != null) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("equipId", be.getEquipId());
                item.put("equipName", equipment.getEquipName());
                item.put("unitPrice", equipment.getUnitPrice());
                item.put("quantity", be.getQuantity());
                item.put("subtotal", equipment.getUnitPrice().multiply(new BigDecimal(be.getQuantity())));
                result.add(item);
            }
        }

        return result;
    }

    // ==================== 辅助方法 ====================

    /**
     * 统一将订单置为指定状态，并释放装备占用（用于取消/结束）
     */
    private void finalizeBooking(Long bookingId, int targetStatus) throws Exception {
        if (bookingId == null) {
            throw new Exception("订单ID不能为空");
        }

        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new Exception("订单不存在");
        }

        if (booking.getStatus() == targetStatus) {
            return; // 已经是目标状态，视为成功
        }

        booking.setStatus(targetStatus);
        booking.setUpdateTime(LocalDateTime.now());
        bookingMapper.update(booking);

        // 删除装备关联 (释放库存)
        bookingEquipMapper.deleteByBookingId(bookingId);
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek().getValue() >= 6;
    }

    private boolean isDayPass(String checkIn, String checkOut) {
        return LocalDate.parse(checkIn).isEqual(LocalDate.parse(checkOut));
    }

    /**
     * 同一天预订视为日营，退出日向后顺延一天用于占用检查
     */
    private LocalDate resolveEffectiveCheckout(String checkIn, String checkOut) {
        LocalDate start = LocalDate.parse(checkIn);
        LocalDate end = LocalDate.parse(checkOut);
        return end.isEqual(start) ? end.plusDays(1) : end;
    }

    private BigDecimal computeDailySitePrice(SiteType siteType, Long typeId, String dateStr) {
        return resolveDayPriceInfo(siteType, typeId, LocalDate.parse(dateStr)).price;
    }

    private DayPriceInfo resolveDayPriceInfo(SiteType siteType, Long typeId, LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ISO_DATE);
        DailyPrice dailyPrice = dailyPriceMapper.selectByTypeAndDate(typeId, dateStr);
        if (dailyPrice != null && dailyPrice.getPrice() != null) {
            return new DayPriceInfo(dateStr, dailyPrice.getPrice(), isWeekend(date), true);
        }
        BigDecimal factor = isWeekend(date) ? WEEKEND_RATE : WEEKDAY_RATE;
        BigDecimal price = siteType.getBasePrice().multiply(factor).setScale(2, RoundingMode.HALF_UP);
        return new DayPriceInfo(dateStr, price, isWeekend(date), false);
    }

    private static class DayPriceInfo {
        final String date;
        final BigDecimal price;
        final boolean weekend;
        final boolean specialPrice;

        DayPriceInfo(String date, BigDecimal price, boolean weekend, boolean specialPrice) {
            this.date = date;
            this.price = price;
            this.weekend = weekend;
            this.specialPrice = specialPrice;
        }
    }
}