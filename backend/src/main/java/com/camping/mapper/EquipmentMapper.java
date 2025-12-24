package com.camping.mapper;

import com.camping.entity.Equipment;
import java.util.List;

/**
 * 装备 Mapper 接口
 */
public interface EquipmentMapper {

    /**
     * 查询所有装备
     */
    List<Equipment> selectAll();

    /**
     * 根据ID查询装备
     */
    Equipment selectById(Long equipId);

    /**
     * 按分类查询装备
     */
    List<Equipment> selectByCategory(String category);

    /**
     * 插入装备
     */
    void insert(Equipment equipment);

    /**
     * 更新装备
     */
    void update(Equipment equipment);

    /**
     * 删除装备
     */
    void delete(Long equipId);
}
