package com.camping.mapper;

import com.camping.entity.Site;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 营位 Mapper 接口
 */
public interface SiteMapper {

    /**
     * 按房型查询营位
     */
    List<Site> selectByTypeId(Long typeId);

    /**
     * 根据ID查询营位
     */
    Site selectById(Long siteId);

    /**
     * 查询可用营位（指定时间内无预订冲突）
     */
    List<Site> selectAvailable(@Param("typeId") Long typeId,
            @Param("checkIn") String checkIn,
            @Param("checkOut") String checkOut);

    /**
     * 插入营位
     */
    void insert(Site site);

    /**
     * 更新营位
     */
    void update(Site site);
}
