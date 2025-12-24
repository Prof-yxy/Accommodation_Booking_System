package com.camping.mapper;

import com.camping.entity.DailyPrice;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 日价格 Mapper 接口
 */
public interface DailyPriceMapper {

    /**
     * 按房型和日期查询价格
     */
    DailyPrice selectByTypeAndDate(@Param("typeId") Long typeId,
            @Param("specificDate") String date);

    /**
     * 按房型和日期范围查询价格
     */
    List<DailyPrice> selectByTypeAndDateRange(@Param("typeId") Long typeId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    /**
     * 插入价格
     */
    void insert(DailyPrice dailyPrice);

    /**
     * 更新价格
     */
    void update(DailyPrice dailyPrice);

    /**
     * 删除价格
     */
    void delete(Long priceId);
}
