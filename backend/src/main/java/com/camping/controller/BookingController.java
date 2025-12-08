package com.camping.controller;

import com.camping.common.Result;
import com.camping.dto.*;
import com.camping.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // 1. 预订前试算与检查 (不锁库存)
    @PostMapping("/check")
    public Result<Map<String, Object>> checkBooking(@RequestBody BookingCheckDTO dto) {
        // 调用 service.calculatePriceAndCheckStock(dto)
        // 返回 {isAvailable, msg, totalPrice, priceDetail}
        return Result.success(null);
    }

    // 2. 提交订单 (核心事务入口)
    @PostMapping("/create")
    public Result<Map<String, Object>> createBooking(@RequestBody BookingCreateDTO dto) {
        // 调用 service.createOrder(dto)
        // 内部需包含: 校验库存 -> 计算价格 -> 分配SiteID -> 落库Booking -> 落库BookingEquip
        // 返回 {bookingId, siteNo, totalPrice}
        return Result.success(null);
    }

    // 3. 支付订单
    @PostMapping("/pay")
    public Result<Map<String, Object>> payBooking(@RequestBody PayDTO dto) {
        // 修改 status 0 -> 1
        return Result.success(null);
    }

    // 4. 我的订单列表
    @GetMapping("/my")
    public Result<List<Object>> myBookings(@RequestParam Long userId,
            @RequestParam(required = false) Integer status) {
        return Result.success(null);
    }

    // 5. 取消订单
    @PostMapping("/cancel")
    public Result<Void> cancelBooking(@RequestBody PayDTO dto) {
        // 重用 PayDTO 中的 bookingId
        // 修改 status -> 2, 触发器或逻辑释放库存
        return Result.success(null);
    }
}