package com.camping.controller;

import com.camping.common.Result;
import com.camping.dto.*;
import com.camping.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 预订模块控制器
 */
@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    /**
     * 预订前检查 - 验证库存和计算价格
     */
    @PostMapping("/check")
    public Result<Map<String, Object>> checkBooking(@RequestBody BookingCheckDTO dto) {
        try {
            Map<String, Object> result = bookingService.checkBooking(dto);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("预订检查失败: " + e.getMessage());
        }
    }

    /**
     * 创建订单 - 核心预订接口
     * 包含事务控制: 校验库存 -> 计算价格 -> 分配营位 -> 保存订单
     */
    @PostMapping("/create")
    public Result<Map<String, Object>> createBooking(@RequestBody BookingCreateDTO dto) {
        try {
            Map<String, Object> result = bookingService.createOrder(dto);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 支付订单
     */
    @PostMapping("/pay")
    public Result<Void> payBooking(@RequestBody PayDTO dto) {
        try {
            bookingService.payBooking(dto.getBookingId());
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("支付失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的订单列表
     */
    @GetMapping("/my")
    public Result<List<Object>> myBookings(@RequestParam Long userId,
            @RequestParam(required = false) Integer status) {
        try {
            var bookings = bookingService.getMyBookings(userId, status);
            return Result.success((List<Object>) (List<?>) bookings);
        } catch (Exception e) {
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{bookingId}")
    public Result<Object> getBookingDetail(@PathVariable Long bookingId) {
        try {
            var booking = bookingService.getBookingDetail(bookingId);
            return Result.success(booking);
        } catch (Exception e) {
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单 - 自动释放装备和营位库存
     */
    @PostMapping("/cancel")
    public Result<Void> cancelBooking(@RequestBody PayDTO dto) {
        try {
            bookingService.cancelBooking(dto.getBookingId());
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单的装备列表
     */
    @GetMapping("/{bookingId}/equipments")
    public Result<List<Object>> getBookingEquipments(@PathVariable Long bookingId) {
        try {
            List<Object> equipments = bookingService.getBookingEquipments(bookingId);
            return Result.success(equipments);
        } catch (Exception e) {
            return Result.error("获取订单装备失败: " + e.getMessage());
        }
    }
}