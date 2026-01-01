package com.camping.service;

import com.camping.dto.PriceSetDTO;
import com.camping.entity.OperationLog;
import java.util.List;
import java.util.Map;

/**
 * 管理员业务接口
 */
public interface AdminService {

    /**
     * 设置日价格
     */
    void setDailyPrice(PriceSetDTO dto) throws Exception;

    /**
     * 获取日收入报表
     */
    Map<String, Object> getDailyReport(String startDate, String endDate) throws Exception;

    /**
     * 获取房型报表
     */
    List<Object> getTypeReport(String startDate, String endDate) throws Exception;

    /**
     * 获取预订统计
     */
    Map<String, Object> getBookingStats() throws Exception;

    /**
     * 获取房型统计
     */
    List<Object> getTypeStats() throws Exception;

    /**
     * 获取操作日志
     */
    Map<String, Object> getOperationLogs(Integer page, Integer pageSize, String operation) throws Exception;

    /**
     * 创建营位
     */
    Map<String, Object> createSite(Long typeId, String siteNo) throws Exception;

    /**
     * 删除营位
     */
    void deleteSite(Long siteId) throws Exception;

    /**
     * 更新营位状态
     */
    void updateSiteStatus(Long siteId, Integer status) throws Exception;
}
