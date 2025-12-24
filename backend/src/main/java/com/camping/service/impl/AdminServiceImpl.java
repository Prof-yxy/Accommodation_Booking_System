package com.camping.service.impl;

import com.camping.dto.PriceSetDTO;
import com.camping.entity.*;
import com.camping.mapper.*;
import com.camping.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Admin service implementation with database operations
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private DailyPriceMapper dailyPriceMapper;

    @Autowired
    private SiteTypeMapper siteTypeMapper;

    @Autowired
    private SiteMapper siteMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * Set daily prices for multiple dates
     */
    @Override
    public void setDailyPrice(PriceSetDTO dto) throws Exception {
        if (dto == null || dto.getTypeId() == null || dto.getDates() == null || dto.getPrice() == null) {
            throw new Exception("Missing required parameters");
        }

        try {
            for (String date : dto.getDates()) {
                DailyPrice existing = dailyPriceMapper.selectByTypeAndDate(dto.getTypeId(), date);
                if (existing != null) {
                    existing.setPrice(dto.getPrice());
                    existing.setUpdateTime(LocalDateTime.now());
                    dailyPriceMapper.update(existing);
                } else {
                    DailyPrice newPrice = new DailyPrice();
                    newPrice.setTypeId(dto.getTypeId());
                    newPrice.setSpecificDate(date);
                    newPrice.setPrice(dto.getPrice());
                    newPrice.setCreateTime(LocalDateTime.now());
                    newPrice.setUpdateTime(LocalDateTime.now());
                    dailyPriceMapper.insert(newPrice);
                }
            }
        } catch (Exception e) {
            throw new Exception("Failed to set price: " + e.getMessage());
        }
    }

    /**
     * Get daily revenue report
     */
    @Override
    public Map<String, Object> getDailyReport(String startDate, String endDate) throws Exception {
        if (startDate == null || endDate == null) {
            throw new Exception("Missing date parameters");
        }

        Map<String, Object> report = new LinkedHashMap<>();

        try {
            List<Booking> allBookings = bookingMapper.selectAll();
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            List<Map<String, Object>> dailyData = new ArrayList<>();
            BigDecimal totalRevenue = BigDecimal.ZERO;
            int totalBookings = 0;

            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                String dateStr = date.format(DateTimeFormatter.ISO_DATE);
                int bookingCount = 0;
                BigDecimal revenue = BigDecimal.ZERO;

                for (Booking booking : allBookings) {
                    if (booking.getStatus() == 2) {
                        LocalDate checkIn = LocalDate.parse(booking.getCheckIn());
                        LocalDate checkOut = LocalDate.parse(booking.getCheckOut());
                        if (!date.isBefore(checkIn) && date.isBefore(checkOut)) {
                            bookingCount++;
                            int nights = (int) (checkOut.toEpochDay() - checkIn.toEpochDay());
                            if (nights > 0) {
                                BigDecimal dailyAmount = booking.getTotalPrice().divide(BigDecimal.valueOf(nights), 2,
                                        BigDecimal.ROUND_HALF_UP);
                                revenue = revenue.add(dailyAmount);
                            }
                        }
                    }
                }

                Map<String, Object> dayData = new LinkedHashMap<>();
                dayData.put("date", dateStr);
                dayData.put("bookingCount", bookingCount);
                dayData.put("revenue", revenue);
                dailyData.add(dayData);

                totalRevenue = totalRevenue.add(revenue);
                totalBookings += bookingCount;
            }

            int days = dailyData.size();
            BigDecimal avgRevenue = days > 0
                    ? totalRevenue.divide(BigDecimal.valueOf(days), 2, BigDecimal.ROUND_HALF_UP)
                    : BigDecimal.ZERO;

            report.put("startDate", startDate);
            report.put("endDate", endDate);
            report.put("totalRevenue", totalRevenue);
            report.put("totalBookings", totalBookings);
            report.put("averageDailyRevenue", avgRevenue);
            report.put("dailyData", dailyData);

            return report;

        } catch (Exception e) {
            throw new Exception("Failed to get daily report: " + e.getMessage());
        }
    }

    /**
     * Get report by site type
     */
    @Override
    public List<Object> getTypeReport(String startDate, String endDate) throws Exception {
        if (startDate == null || endDate == null) {
            throw new Exception("Missing date parameters");
        }

        List<Object> report = new ArrayList<>();

        try {
            List<SiteType> types = siteTypeMapper.selectAll();
            List<Booking> allBookings = bookingMapper.selectAll();
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            for (SiteType type : types) {
                int bookingCount = 0;
                BigDecimal revenue = BigDecimal.ZERO;

                for (Booking booking : allBookings) {
                    if (booking.getStatus() == 2 && type.getTypeId().equals(booking.getTypeId())) {
                        LocalDate checkIn = LocalDate.parse(booking.getCheckIn());
                        if (!checkIn.isBefore(start) && !checkIn.isAfter(end)) {
                            bookingCount++;
                            revenue = revenue.add(booking.getTotalPrice());
                        }
                    }
                }

                Map<String, Object> typeReport = new LinkedHashMap<>();
                typeReport.put("typeId", type.getTypeId());
                typeReport.put("typeName", type.getTypeName());
                typeReport.put("bookingCount", bookingCount);
                typeReport.put("revenue", revenue);
                report.add(typeReport);
            }

            return report;

        } catch (Exception e) {
            throw new Exception("Failed to get type report: " + e.getMessage());
        }
    }

    /**
     * Get booking statistics
     */
    @Override
    public Map<String, Object> getBookingStats() throws Exception {
        Map<String, Object> stats = new LinkedHashMap<>();

        try {
            List<Booking> allBookings = bookingMapper.selectAll();

            int totalBookings = allBookings.size();
            int pendingBookings = 0;
            int paidBookings = 0;
            int cancelledBookings = 0;
            BigDecimal totalRevenue = BigDecimal.ZERO;

            for (Booking booking : allBookings) {
                if (booking.getStatus() == 1)
                    pendingBookings++;
                else if (booking.getStatus() == 2) {
                    paidBookings++;
                    totalRevenue = totalRevenue.add(booking.getTotalPrice());
                } else if (booking.getStatus() == 3)
                    cancelledBookings++;
            }

            stats.put("totalBookings", totalBookings);
            stats.put("pendingBookings", pendingBookings);
            stats.put("paidBookings", paidBookings);
            stats.put("cancelledBookings", cancelledBookings);
            stats.put("totalRevenue", totalRevenue);

            return stats;

        } catch (Exception e) {
            throw new Exception("Failed to get booking stats: " + e.getMessage());
        }
    }

    /**
     * Get site type statistics
     */
    @Override
    public List<Object> getTypeStats() throws Exception {
        List<Object> stats = new ArrayList<>();

        try {
            List<SiteType> types = siteTypeMapper.selectAll();

            for (SiteType type : types) {
                List<Site> sites = siteMapper.selectByTypeId(type.getTypeId());
                int availableCount = 0;
                int occupiedCount = 0;

                for (Site site : sites) {
                    if (site.getStatus() == 1)
                        availableCount++;
                    else
                        occupiedCount++;
                }

                Map<String, Object> typeStat = new LinkedHashMap<>();
                typeStat.put("typeId", type.getTypeId());
                typeStat.put("typeName", type.getTypeName());
                typeStat.put("totalSites", sites.size());
                typeStat.put("availableSites", availableCount);
                typeStat.put("occupiedSites", occupiedCount);
                typeStat.put("basePrice", type.getBasePrice());
                stats.add(typeStat);
            }

            return stats;

        } catch (Exception e) {
            throw new Exception("Failed to get type stats: " + e.getMessage());
        }
    }

    /**
     * Get operation logs with pagination
     */
    @Override
    public Map<String, Object> getOperationLogs(Integer page, Integer pageSize, String operation) throws Exception {
        if (page == null || pageSize == null) {
            throw new Exception("Missing pagination parameters");
        }

        Map<String, Object> result = new LinkedHashMap<>();

        try {
            List<OperationLog> logs;
            if (operation != null && !operation.isEmpty()) {
                logs = operationLogMapper.selectByOperation(operation);
            } else {
                Map<String, Object> params = new HashMap<>();
                logs = operationLogMapper.selectAll(params);
            }

            int total = logs.size();
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, total);

            List<Object> items = new ArrayList<>();
            if (startIndex < total) {
                for (int i = startIndex; i < endIndex; i++) {
                    OperationLog log = logs.get(i);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("logId", log.getLogId());
                    item.put("operation", log.getOperation());
                    item.put("operatorId", log.getOperatorId());
                    item.put("operatorName", log.getOperatorName());
                    item.put("description", log.getDescription());
                    item.put("details", log.getDetails());
                    item.put("logTime", log.getLogTime());
                    items.add(item);
                }
            }

            result.put("items", items);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (total + pageSize - 1) / pageSize);

            return result;

        } catch (Exception e) {
            throw new Exception("Failed to get operation logs: " + e.getMessage());
        }
    }

    /**
     * Update site status
     */
    @Override
    public void updateSiteStatus(Long siteId, Integer status) throws Exception {
        if (siteId == null || status == null) {
            throw new Exception("Missing required parameters");
        }

        if (status != 0 && status != 1) {
            throw new Exception("Invalid status value, must be 0 (maintenance) or 1 (normal)");
        }

        try {
            Site site = siteMapper.selectById(siteId);
            if (site == null) {
                throw new Exception("Site not found");
            }

            site.setStatus(status);
            site.setUpdateTime(LocalDateTime.now());
            siteMapper.update(site);

            // Log operation
            OperationLog log = new OperationLog(
                    "UPDATE_SITE_STATUS",
                    null,
                    "SYSTEM",
                    "Update site status to: " + (status == 1 ? "Normal" : "Maintenance"),
                    "siteId=" + siteId + ", newStatus=" + status,
                    LocalDateTime.now());
            operationLogMapper.insert(log);

        } catch (Exception e) {
            throw new Exception("Failed to update site status: " + e.getMessage());
        }
    }
}
