package com.camping.mapper;

import com.camping.entity.SiteType;
import java.util.List;

/**
 * 房型 Mapper 接口
 */
public interface SiteTypeMapper {

    /**
     * 查询所有房型
     */
    List<SiteType> selectAll();

    /**
     * 根据ID查询房型
     */
    SiteType selectById(Long typeId);

    /**
     * 按名称查询
     */
    SiteType selectByName(String typeName);

    /**
     * 插入房型
     */
    void insert(SiteType siteType);

    /**
     * 更新房型
     */
    void update(SiteType siteType);

    /**
     * 删除房型
     */
    void delete(Long typeId);

    /** 删除全部房型 */
    void deleteAll();
}
