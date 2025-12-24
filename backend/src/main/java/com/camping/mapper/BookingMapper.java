package com.camping.mapper;

import com.camping.entity.Booking;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 预订 Mapper 接口
 */
public interface BookingMapper {

    /**
     * 根据ID查询预订
     */
    Booking selectById(Long bookingId);

    /**
     * 按用户查询预订
     */
    List<Booking> selectByUserId(Long userId);

    /**
     * 按用户和状态查询预订
     */
    List<Booking> selectByUserIdAndStatus(@Param("userId") Long userId,
            @Param("status") Integer status);

    /**
     * 查询时间段内的冲突预订
     */
    List<Booking> selectConflict(@Param("typeId") Long typeId,
            @Param("checkIn") String checkIn,
            @Param("checkOut") String checkOut);

    /**
     * 插入预订
     */
    void insert(Booking booking);

    /**
     * 更新预订
     */
    void update(Booking booking);

    /**
     * 查询所有预订
     */
    List<Booking> selectAll();
}
