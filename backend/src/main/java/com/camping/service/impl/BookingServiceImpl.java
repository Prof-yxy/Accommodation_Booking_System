package com.camping.service.impl;

import com.camping.dto.BookingCheckDTO;
import com.camping.dto.BookingCreateDTO;
import com.camping.dto.EquipSelectDTO;
import com.camping.entity.*;
import com.camping.service.BookingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 预订业务实现
 */
@Service
public class BookingServiceImpl implements BookingService {

    /**
     * 预订前检查 - 不锁库存，仅计算价格
     */
    @Override
    public Map<String, Object> checkBooking(BookingCheckDTO dto) throws Exception {
        if (dto.getTypeId() == null || dto.getCheckIn() == null || dto.getCheckOut() == null) {
            throw new Exception("参数不完整");
        }

        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 查询房型信息
            // SiteType siteType = siteTypeMapper.selectById(dto.getTypeId());
            SiteType siteType = new SiteType();
            siteType.setTypeId(dto.getTypeId());
            siteType.setBasePrice(new BigDecimal("100.00"));

            if (siteType == null) {
                result.put("isAvailable", false);
                result.put("msg", "房型不存在");
                return result;
            }

            // 2. 计算天数和基础价格
            int nights = calculateNights(dto.getCheckIn(), dto.getCheckOut());
            BigDecimal basePrice = siteType.getBasePrice().multiply(new BigDecimal(nights));

            // 3. 查询日价格并累计
            // List<DailyPrice> dailyPrices = dailyPriceMapper.selectByTypeAndDates(...)
            List<Map<String, Object>> priceDetail = new ArrayList<>();
            BigDecimal dynamicPrice = BigDecimal.ZERO;

            // TODO: 遍历每一天，查询浮动价格

            // 4. 计算装备价格
            BigDecimal equipmentPrice = BigDecimal.ZERO;
            if (dto.getEquipments() != null && !dto.getEquipments().isEmpty()) {
                for (EquipSelectDTO equip : dto.getEquipments()) {
                    // Equipment equipment = equipmentMapper.selectById(equip.getEquipId());
                    Equipment equipment = new Equipment();
                    equipment.setUnitPrice(new BigDecimal("50.00"));

                    if (equipment != null) {
                        BigDecimal equipCost = equipment.getUnitPrice().multiply(new BigDecimal(equip.getCount()));
                        equipmentPrice = equipmentPrice.add(equipCost);
                    }
                }
            }

            // 5. 验证库存充足性
            boolean stockAvailable = checkEquipmentStock(dto.getEquipments());

            // 6. 计算总价
            BigDecimal totalPrice = basePrice.add(dynamicPrice).add(equipmentPrice);

            result.put("isAvailable", stockAvailable);
            result.put("msg", stockAvailable ? "可预订" : "库存不足");
            result.put("totalPrice", totalPrice);

            Map<String, Object> priceDetailMap = new HashMap<>();
            priceDetailMap.put("basePrice", basePrice);
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
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 参数验证
            if (dto.getUserId() == null || dto.getTypeId() == null) {
                throw new Exception("用户ID或房型ID不能为空");
            }

            // 2. 装备库存检查 (文档 Page 10 SQL逻辑)
            // 遍历装备，检查库存是否充足
            if (dto.getEquipments() != null && !dto.getEquipments().isEmpty()) {
                for (EquipSelectDTO equip : dto.getEquipments()) {
                    // SQL: SELECT SUM(quantity) FROM BookingEquipTable
                    // WHERE equipId = ? AND bookingId IN (SELECT bookingId FROM BookingTable
                    // WHERE checkIn < ? AND checkOut > ? AND status != 2)
                    int usedQuantity = getUsedEquipmentQuantity(equip.getEquipId(),
                            dto.getCheckIn(),
                            dto.getCheckOut());

                    // Equipment equipment = equipmentMapper.selectById(equip.getEquipId());
                    Equipment equipment = new Equipment();
                    equipment.setTotalStock(100);

                    int available = equipment.getTotalStock() - usedQuantity;
                    if (available < equip.getCount()) {
                        throw new Exception("装备库存不足: " + equip.getEquipId());
                    }
                }
            }

            // 3. 价格计算 (不使用前端传来的价格)
            BigDecimal totalPrice = calculateTotalPrice(dto);

            // 4. 营位自动分配
            // 查询该房型所有正常营位
            // 排除时间冲突的营位
            Long siteId = allocateSite(dto.getTypeId(), dto.getCheckIn(), dto.getCheckOut());

            if (siteId == null) {
                throw new Exception("暂无可用营位");
            }

            // 5. 数据落库
            Booking booking = new Booking();
            booking.setUserId(dto.getUserId());
            booking.setTypeId(dto.getTypeId());
            booking.setSiteId(siteId);
            booking.setCheckIn(dto.getCheckIn());
            booking.setCheckOut(dto.getCheckOut());
            booking.setGuestName(dto.getGuestName());
            booking.setGuestPhone(dto.getGuestPhone());
            booking.setTotalPrice(totalPrice);
            booking.setStatus(0); // 0: 待支付
            booking.setCreateTime(LocalDateTime.now());

            // TODO: bookingMapper.insert(booking);
            Long bookingId = 1L; // 模拟返回的ID

            // 6. 保存装备关联
            if (dto.getEquipments() != null && !dto.getEquipments().isEmpty()) {
                for (EquipSelectDTO equip : dto.getEquipments()) {
                    BookingEquip bookingEquip = new BookingEquip();
                    bookingEquip.setBookingId(bookingId);
                    bookingEquip.setEquipId(equip.getEquipId());
                    bookingEquip.setQuantity(equip.getCount());
                    bookingEquip.setCreateTime(LocalDateTime.now());

                    // TODO: bookingEquipMapper.insert(bookingEquip);
                }
            }

            // 7. 返回结果
            result.put("bookingId", bookingId);
            result.put("siteNo", "A-101"); // 从 site 对象获取
            result.put("totalPrice", totalPrice);

            return result;

        } catch (Exception e) {
            throw new Exception("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 支付订单
     */
    @Override
    public void payBooking(Long bookingId) throws Exception {
        if (bookingId == null) {
            throw new Exception("订单ID不能为空");
        }

        // TODO: Booking booking = bookingMapper.selectById(bookingId);
        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        booking.setStatus(0);

        if (booking == null) {
            throw new Exception("订单不存在");
        }

        if (booking.getStatus() != 0) {
            throw new Exception("订单状态无效");
        }

        booking.setStatus(1); // 1: 已支付
        booking.setUpdateTime(LocalDateTime.now());

        // TODO: bookingMapper.updateById(booking);
    }

    /**
     * 获取我的订单
     */
    @Override
    public List<Booking> getMyBookings(Long userId, Integer status) throws Exception {
        if (userId == null) {
            throw new Exception("用户ID不能为空");
        }

        // TODO: if (status != null) {
        // return bookingMapper.selectList(new QueryWrapper<Booking>()
        // .eq("userId", userId)
        // .eq("status", status));
        // }
        // return bookingMapper.selectList(new QueryWrapper<Booking>().eq("userId",
        // userId));

        return new ArrayList<>();
    }

    /**
     * 获取订单详情
     */
    @Override
    public Booking getBookingDetail(Long bookingId) throws Exception {
        if (bookingId == null) {
            throw new Exception("订单ID不能为空");
        }

        // TODO: return bookingMapper.selectById(bookingId);
        return new Booking();
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

        // TODO: Booking booking = bookingMapper.selectById(bookingId);
        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        booking.setStatus(0);

        if (booking == null) {
            throw new Exception("订单不存在");
        }

        if (booking.getStatus() == 2) {
            throw new Exception("订单已取消");
        }

        booking.setStatus(2); // 2: 已取消
        booking.setUpdateTime(LocalDateTime.now());

        // TODO: bookingMapper.updateById(booking);

        // 触发器会自动释放库存
    }

    /**
     * 获取订单的装备列表
     */
    @Override
    public List<Object> getBookingEquipments(Long bookingId) throws Exception {
        if (bookingId == null) {
            throw new Exception("订单ID不能为空");
        }

        // TODO: 查询订单关联的装备
        // List<BookingEquip> bookingEquips =
        // bookingEquipMapper.selectByBookingId(bookingId);
        // 对每个 BookingEquip，关联查询 Equipment 表获取装备详情
        // 返回: [{ equipId, equipName, unitPrice, quantity, subtotal }]

        return new ArrayList<>();
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算入住天数
     */
    private int calculateNights(String checkIn, String checkOut) {
        // TODO: 使用日期工具类计算
        return 3; // 模拟
    }

    /**
     * 检查装备库存
     */
    private boolean checkEquipmentStock(List<EquipSelectDTO> equipments) {
        if (equipments == null || equipments.isEmpty()) {
            return true;
        }

        for (EquipSelectDTO equip : equipments) {
            // TODO: 查询总库存和已使用库存
            int totalStock = 100;
            int usedStock = 30;

            if (totalStock - usedStock < equip.getCount()) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取已使用的装备数量
     */
    private int getUsedEquipmentQuantity(Long equipId, String checkIn, String checkOut) {
        // TODO: SQL查询
        // SELECT SUM(quantity) FROM BookingEquipTable
        // WHERE equipId = ? AND bookingId IN (
        // SELECT bookingId FROM BookingTable
        // WHERE checkIn < ? AND checkOut > ? AND status != 2
        // )
        return 0; // 模拟
    }

    /**
     * 计算总价格
     */
    private BigDecimal calculateTotalPrice(BookingCreateDTO dto) {
        // TODO: 完整的价格计算逻辑
        return new BigDecimal("500.00"); // 模拟
    }

    /**
     * 分配营位
     */
    private Long allocateSite(Long typeId, String checkIn, String checkOut) {
        // TODO: 查询房型的所有营位
        // TODO: 排除时间冲突的营位
        // TODO: 返回第一个可用的营位ID
        return 1L; // 模拟
    }
}