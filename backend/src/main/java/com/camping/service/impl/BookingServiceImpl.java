package com.camping.service.impl;

import com.camping.dto.BookingCheckDTO;
import com.camping.dto.BookingCreateDTO;
import com.camping.dto.EquipSelectDTO;
import com.camping.entity.*;
import com.camping.mapper.*;
import com.camping.service.BookingService;
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
                result.put("msg", "房型不存在");
                return result;
            }

            // 2. 计算天数
            int nights = calculateNights(dto.getCheckIn(), dto.getCheckOut());

            // 3. 查询日价格并累计
            List<Map<String, Object>> priceDetail = new ArrayList<>();
            BigDecimal sitePrice = BigDecimal.ZERO;

            LocalDate startDate = LocalDate.parse(dto.getCheckIn());
            LocalDate endDate = LocalDate.parse(dto.getCheckOut());

            for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
                String dateStr = date.format(DateTimeFormatter.ISO_DATE);
                DailyPrice dailyPrice = dailyPriceMapper.selectByTypeAndDate(dto.getTypeId(), dateStr);

                BigDecimal dayPrice;
                if (dailyPrice != null && dailyPrice.getPrice() != null) {
                    dayPrice = dailyPrice.getPrice();
                } else {
                    dayPrice = siteType.getBasePrice();
                }

                sitePrice = sitePrice.add(dayPrice);

                Map<String, Object> dayDetail = new LinkedHashMap<>();
                dayDetail.put("date", dateStr);
                dayDetail.put("price", dayPrice);
                priceDetail.add(dayDetail);
            }

            // 4. 检查营位可用性
            List<Site> availableSites = siteMapper.selectAvailable(dto.getTypeId(), dto.getCheckIn(),
                    dto.getCheckOut());
            boolean siteAvailable = availableSites != null && !availableSites.isEmpty();

            // 5. 计算装备价格并检查库存
            BigDecimal equipmentPrice = BigDecimal.ZERO;
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

                        // 检查库存
                        Integer usedCount = bookingEquipMapper.sumQuantityByEquipAndDate(
                                equip.getEquipId(), dto.getCheckIn(), dto.getCheckOut());
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

            // 2. 查询房型
            SiteType siteType = siteTypeMapper.selectById(dto.getTypeId());
            if (siteType == null) {
                throw new Exception("房型不存在");
            }

            // 3. 装备库存检查
            if (dto.getEquipments() != null && !dto.getEquipments().isEmpty()) {
                for (EquipSelectDTO equip : dto.getEquipments()) {
                    Equipment equipment = equipmentMapper.selectById(equip.getEquipId());
                    if (equipment == null) {
                        throw new Exception("装备不存在: " + equip.getEquipId());
                    }

                    Integer usedCount = bookingEquipMapper.sumQuantityByEquipAndDate(
                            equip.getEquipId(), dto.getCheckIn(), dto.getCheckOut());
                    int used = usedCount != null ? usedCount : 0;
                    int available = (equipment.getTotalStock() != null ? equipment.getTotalStock() : 0) - used;

                    if (available < equip.getCount()) {
                        throw new Exception("装备库存不足: " + equipment.getEquipName());
                    }
                }
            }

            // 4. 营位自动分配
            List<Site> availableSites = siteMapper.selectAvailable(dto.getTypeId(), dto.getCheckIn(),
                    dto.getCheckOut());
            if (availableSites == null || availableSites.isEmpty()) {
                throw new Exception("暂无可用营位");
            }
            Site allocatedSite = availableSites.get(0);

            // 5. 价格计算 (服务端计算，不信任前端传来的价格)
            int nights = calculateNights(dto.getCheckIn(), dto.getCheckOut());
            BigDecimal sitePrice = BigDecimal.ZERO;

            LocalDate startDate = LocalDate.parse(dto.getCheckIn());
            LocalDate endDate = LocalDate.parse(dto.getCheckOut());

            for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
                String dateStr = date.format(DateTimeFormatter.ISO_DATE);
                DailyPrice dailyPrice = dailyPriceMapper.selectByTypeAndDate(dto.getTypeId(), dateStr);

                if (dailyPrice != null && dailyPrice.getPrice() != null) {
                    sitePrice = sitePrice.add(dailyPrice.getPrice());
                } else {
                    sitePrice = sitePrice.add(siteType.getBasePrice());
                }
            }

            BigDecimal equipmentPrice = BigDecimal.ZERO;
            if (dto.getEquipments() != null && !dto.getEquipments().isEmpty()) {
                for (EquipSelectDTO equip : dto.getEquipments()) {
                    Equipment equipment = equipmentMapper.selectById(equip.getEquipId());
                    if (equipment != null) {
                        BigDecimal equipCost = equipment.getUnitPrice()
                                .multiply(new BigDecimal(equip.getCount()))
                                .multiply(new BigDecimal(nights));
                        equipmentPrice = equipmentPrice.add(equipCost);
                    }
                }
            }

            BigDecimal totalPrice = sitePrice.add(equipmentPrice);

            // 6. 创建订单对象
            Booking booking = new Booking();
            booking.setUserId(dto.getUserId());
            booking.setTypeId(dto.getTypeId());
            booking.setSiteId(allocatedSite.getSiteId());
            booking.setCheckIn(dto.getCheckIn());
            booking.setCheckOut(dto.getCheckOut());
            booking.setGuestName(dto.getGuestName());
            booking.setGuestPhone(dto.getGuestPhone());
            booking.setTotalPrice(totalPrice);
            booking.setStatus(1); // 1: 待支付
            booking.setCreateTime(LocalDateTime.now());

            bookingMapper.insert(booking);
            Long bookingId = booking.getBookingId();

            // 7. 保存装备关联
            if (dto.getEquipments() != null && !dto.getEquipments().isEmpty()) {
                for (EquipSelectDTO equip : dto.getEquipments()) {
                    BookingEquip bookingEquip = new BookingEquip();
                    bookingEquip.setBookingId(bookingId);
                    bookingEquip.setEquipId(equip.getEquipId());
                    bookingEquip.setQuantity(equip.getCount());

                    bookingEquipMapper.insert(bookingEquip);
                }
            }

            // 8. 返回结果
            result.put("bookingId", bookingId);
            result.put("siteNo", allocatedSite.getSiteNo());
            result.put("totalPrice", totalPrice);
            result.put("status", booking.getStatus());

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

        if (booking.getStatus() != 1) { // 1: 待支付
            throw new Exception("订单状态无效，无法支付");
        }

        booking.setStatus(2); // 2: 已支付
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

    /**
     * 取消订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long bookingId) throws Exception {
        if (bookingId == null) {
            throw new Exception("订单ID不能为空");
        }

        Booking booking = bookingMapper.selectById(bookingId);

        if (booking == null) {
            throw new Exception("订单不存在");
        }

        if (booking.getStatus() == 3) { // 3: 已取消
            throw new Exception("订单已取消");
        }

        booking.setStatus(3); // 3: 已取消
        booking.setUpdateTime(LocalDateTime.now());

        bookingMapper.update(booking);

        // 删除装备关联 (释放库存)
        bookingEquipMapper.deleteByBookingId(bookingId);
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
     * 计算入住天数
     */
    private int calculateNights(String checkIn, String checkOut) {
        LocalDate start = LocalDate.parse(checkIn);
        LocalDate end = LocalDate.parse(checkOut);
        return (int) ChronoUnit.DAYS.between(start, end);
    }
}