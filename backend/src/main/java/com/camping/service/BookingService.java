package com.camping.service;

import com.camping.dto.BookingCheckDTO;
import com.camping.dto.BookingCreateDTO;
import com.camping.entity.Booking;
import java.util.List;
import java.util.Map;

/**
 * 预订业务接口
 */
public interface BookingService {

    /**
     * 预订前检查 - 验证库存和计算价格
     */
    Map<String, Object> checkBooking(BookingCheckDTO dto) throws Exception;

    /**
     * 创建订单 - 核心事务方法
     * 包含: 校验库存 -> 计算价格 -> 分配营位 -> 落库
     */
    Map<String, Object> createOrder(BookingCreateDTO dto) throws Exception;

    /**
     * 支付订单
     */
    void payBooking(Long bookingId) throws Exception;

    /**
     * 获取我的订单
     */
    List<Booking> getMyBookings(Long userId, Integer status) throws Exception;

    /**
     * 获取订单详情
     */
    Booking getBookingDetail(Long bookingId) throws Exception;

    /**
     * 取消订单
     */
    void cancelBooking(Long bookingId) throws Exception;

    /**
     * 获取订单的装备列表
     */
    List<Object> getBookingEquipments(Long bookingId) throws Exception;
}