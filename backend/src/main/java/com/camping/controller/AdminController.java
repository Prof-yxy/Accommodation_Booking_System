package com.camping.controller;

import com.camping.common.Result;
import com.camping.dto.PriceSetDTO;
import com.camping.entity.*;
import com.camping.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Admin Module Controller
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private DailyPriceMapper dailyPriceMapper;

    @Autowired
    private SiteTypeMapper siteTypeMapper;

    @Autowired
    private SiteMapper siteMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * Set daily price for multiple dates
     */
    @PostMapping("/price/set")
    public Result<Void> setDailyPrice(@RequestBody PriceSetDTO dto) {
        try {
            List<String> dates = dto.getDates();
            if (dates == null || dates.isEmpty()) {
                return Result.error("No dates provided");
            }
            for (String dateStr : dates) {
                DailyPrice existing = dailyPriceMapper.selectByTypeAndDate(dto.getTypeId(), dateStr);
                if (existing != null) {
                    existing.setPrice(dto.getPrice());
                    dailyPriceMapper.update(existing);
                } else {
                    DailyPrice newPrice = new DailyPrice();
                    newPrice.setTypeId(dto.getTypeId());
                    newPrice.setSpecificDate(dateStr);
                    newPrice.setPrice(dto.getPrice());
                    dailyPriceMapper.insert(newPrice);
                }
            }
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Failed to set price: " + e.getMessage());
        }
    }

    /**
     * Batch set daily prices for date range
     */
    @PostMapping("/price/batch")
    public Result<Void> setDailyPricesBatch(@RequestBody Map<String, Object> data) {
        try {
            Long typeId = ((Number) data.get("typeId")).longValue();
            String startDate = (String) data.get("startDate");
            String endDate = (String) data.get("endDate");
            BigDecimal price = new BigDecimal(data.get("price").toString());

            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                String dateStr = date.format(DateTimeFormatter.ISO_DATE);
                DailyPrice existing = dailyPriceMapper.selectByTypeAndDate(typeId, dateStr);
                if (existing != null) {
                    existing.setPrice(price);
                    dailyPriceMapper.update(existing);
                } else {
                    DailyPrice newPrice = new DailyPrice();
                    newPrice.setTypeId(typeId);
                    newPrice.setSpecificDate(dateStr);
                    newPrice.setPrice(price);
                    dailyPriceMapper.insert(newPrice);
                }
            }
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Failed to batch set prices: " + e.getMessage());
        }
    }

    /**
     * Get daily revenue report
     */
    @GetMapping("/report/daily")
    public Result<Object> getDailyReport(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            List<Booking> allBookings = bookingMapper.selectAll();
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            List<Object> report = new ArrayList<>();
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

                Map<String, Object> dayReport = new LinkedHashMap<>();
                dayReport.put("date", dateStr);
                dayReport.put("bookingCount", bookingCount);
                dayReport.put("revenue", revenue);
                report.add(dayReport);
            }
            return Result.success(report);
        } catch (Exception e) {
            return Result.error("Failed to get daily report: " + e.getMessage());
        }
    }

    /**
     * Get report by site type
     */
    @GetMapping("/report/type")
    public Result<Object> getTypeReport(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            List<SiteType> types = siteTypeMapper.selectAll();
            List<Booking> allBookings = bookingMapper.selectAll();
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            List<Object> report = new ArrayList<>();
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
            return Result.success(report);
        } catch (Exception e) {
            return Result.error("Failed to get type report: " + e.getMessage());
        }
    }

    /**
     * Get booking statistics
     */
    @GetMapping("/stats/booking")
    public Result<Object> getBookingStats() {
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

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalBookings", totalBookings);
            stats.put("pendingBookings", pendingBookings);
            stats.put("paidBookings", paidBookings);
            stats.put("cancelledBookings", cancelledBookings);
            stats.put("totalRevenue", totalRevenue);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("Failed to get booking stats: " + e.getMessage());
        }
    }

    /**
     * Get site type statistics
     */
    @GetMapping("/stats/type")
    public Result<Object> getTypeStats() {
        try {
            List<SiteType> types = siteTypeMapper.selectAll();
            List<Object> stats = new ArrayList<>();

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
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("Failed to get type stats: " + e.getMessage());
        }
    }

    /**
     * Get operation logs with pagination
     */
    @GetMapping("/logs/operation")
    public Result<Object> getOperationLogs(@RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String operation) {
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

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("items", items);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("Failed to get operation logs: " + e.getMessage());
        }
    }

    /**
     * Get all sites with optional type filter
     */
    @GetMapping("/sites")
    public Result<List<Object>> getAllSites(@RequestParam(required = false) Long typeId) {
        try {
            List<Site> sites;
            if (typeId != null) {
                sites = siteMapper.selectByTypeId(typeId);
            } else {
                List<SiteType> types = siteTypeMapper.selectAll();
                sites = new ArrayList<>();
                for (SiteType type : types) {
                    sites.addAll(siteMapper.selectByTypeId(type.getTypeId()));
                }
            }

            List<Object> result = new ArrayList<>();
            for (Site site : sites) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("siteId", site.getSiteId());
                item.put("typeId", site.getTypeId());
                item.put("siteNo", site.getSiteNo());
                item.put("status", site.getStatus());

                SiteType type = siteTypeMapper.selectById(site.getTypeId());
                item.put("typeName", type != null ? type.getTypeName() : "Unknown");
                result.add(item);
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("Failed to get sites: " + e.getMessage());
        }
    }

    /**
     * Get site detail
     */
    @GetMapping("/site/{siteId}")
    public Result<Object> getSiteDetail(@PathVariable Long siteId) {
        try {
            Site site = siteMapper.selectById(siteId);
            if (site == null) {
                return Result.error("Site not found");
            }

            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("siteId", site.getSiteId());
            detail.put("typeId", site.getTypeId());
            detail.put("siteNo", site.getSiteNo());
            detail.put("status", site.getStatus());

            SiteType type = siteTypeMapper.selectById(site.getTypeId());
            if (type != null) {
                detail.put("typeName", type.getTypeName());
                detail.put("basePrice", type.getBasePrice());
                detail.put("maxGuests", type.getMaxGuests());
            }
            return Result.success(detail);
        } catch (Exception e) {
            return Result.error("Failed to get site detail: " + e.getMessage());
        }
    }

    /**
     * Update site status
     */
    @PutMapping("/site/{siteId}/status")
    public Result<Void> updateSiteStatus(@PathVariable Long siteId, @RequestBody Map<String, Integer> data) {
        try {
            Site site = siteMapper.selectById(siteId);
            if (site == null) {
                return Result.error("Site not found");
            }

            Integer newStatus = data.get("status");
            if (newStatus == null) {
                return Result.error("Missing status parameter");
            }

            site.setStatus(newStatus);
            siteMapper.update(site);

            // Log operation
            OperationLog log = new OperationLog(
                    "UPDATE_SITE_STATUS",
                    null,
                    "SYSTEM",
                    "Update site status to: " + (newStatus == 1 ? "Normal" : "Maintenance"),
                    "siteId=" + siteId + ", newStatus=" + newStatus,
                    LocalDateTime.now());
            operationLogMapper.insert(log);

            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Failed to update site status: " + e.getMessage());
        }
    }

    /**
     * Get occupancy by date
     */
    @GetMapping("/occupancy/date")
    public Result<Object> getOccupancyByDate(@RequestParam String date, @RequestParam(required = false) Long typeId) {
        try {
            List<SiteType> types;
            if (typeId != null) {
                SiteType type = siteTypeMapper.selectById(typeId);
                types = type != null ? Collections.singletonList(type) : Collections.emptyList();
            } else {
                types = siteTypeMapper.selectAll();
            }

            int totalCount = 0;
            int occupiedCount = 0;

            for (SiteType type : types) {
                List<Site> allSites = siteMapper.selectByTypeId(type.getTypeId());
                List<Site> availableSites = siteMapper.selectAvailable(type.getTypeId(), date, date);
                totalCount += allSites.size();
                occupiedCount += allSites.size() - (availableSites != null ? availableSites.size() : 0);
            }

            double occupancyRate = totalCount > 0 ? (double) occupiedCount / totalCount : 0;

            Map<String, Object> occupancy = new LinkedHashMap<>();
            occupancy.put("date", date);
            occupancy.put("occupiedCount", occupiedCount);
            occupancy.put("totalCount", totalCount);
            occupancy.put("availableCount", totalCount - occupiedCount);
            occupancy.put("occupancyRate", Math.round(occupancyRate * 100) / 100.0);
            return Result.success(occupancy);
        } catch (Exception e) {
            return Result.error("Failed to get occupancy: " + e.getMessage());
        }
    }

    /**
     * Get revenue trend
     */
    @GetMapping("/revenue/trend")
    public Result<Object> getRevenueTrend(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            List<Booking> allBookings = bookingMapper.selectAll();
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            List<Object> trend = new ArrayList<>();
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                BigDecimal dailyRevenue = BigDecimal.ZERO;
                int bookingCount = 0;

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
                                dailyRevenue = dailyRevenue.add(dailyAmount);
                            }
                        }
                    }
                }

                Map<String, Object> dayData = new LinkedHashMap<>();
                dayData.put("date", date.format(DateTimeFormatter.ISO_DATE));
                dayData.put("revenue", dailyRevenue);
                dayData.put("bookingCount", bookingCount);
                trend.add(dayData);
            }
            return Result.success(trend);
        } catch (Exception e) {
            return Result.error("Failed to get revenue trend: " + e.getMessage());
        }
    }

    /**
     * Adjust booking price
     */
    @PutMapping("/booking/{bookingId}/price")
    public Result<Void> adjustBookingPrice(@PathVariable Long bookingId, @RequestBody Map<String, Object> data) {
        try {
            Booking booking = bookingMapper.selectById(bookingId);
            if (booking == null) {
                return Result.error("Booking not found");
            }

            BigDecimal newPrice = new BigDecimal(data.get("price").toString());
            BigDecimal oldPrice = booking.getTotalPrice();
            booking.setTotalPrice(newPrice);
            bookingMapper.update(booking);

            // Log operation
            OperationLog log = new OperationLog(
                    "ADJUST_PRICE",
                    null,
                    "ADMIN",
                    "Adjust booking price from " + oldPrice + " to " + newPrice,
                    "bookingId=" + bookingId,
                    LocalDateTime.now());
            operationLogMapper.insert(log);

            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Failed to adjust price: " + e.getMessage());
        }
    }

    /**
     * Get user behavior log
     */
    @GetMapping("/logs/user-behavior")
    public Result<Object> getUserBehaviorLog(@RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            Map<String, Object> params = new HashMap<>();
            List<OperationLog> logs = operationLogMapper.selectAll(params);

            if (userId != null) {
                logs.removeIf(log -> !userId.equals(log.getOperatorId()));
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
                    item.put("description", log.getDescription());
                    item.put("logTime", log.getLogTime());
                    items.add(item);
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("items", items);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("Failed to get user behavior log: " + e.getMessage());
        }
    }

    // ========== Site Type CRUD ==========

    /**
     * Create site type
     */
    @PostMapping("/type")
    public Result<Object> createSiteType(@RequestBody SiteType siteType) {
        try {
            siteType.setCreateTime(LocalDateTime.now());
            siteType.setUpdateTime(LocalDateTime.now());
            siteTypeMapper.insert(siteType);

            // Log operation
            OperationLog log = new OperationLog(
                    "CREATE_SITE_TYPE",
                    null,
                    "ADMIN",
                    "Create site type: " + siteType.getTypeName(),
                    "typeId=" + siteType.getTypeId(),
                    LocalDateTime.now());
            operationLogMapper.insert(log);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("typeId", siteType.getTypeId());
            result.put("typeName", siteType.getTypeName());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("Failed to create site type: " + e.getMessage());
        }
    }

    /**
     * Update site type
     */
    @PutMapping("/type/{typeId}")
    public Result<Void> updateSiteType(@PathVariable Long typeId, @RequestBody SiteType siteType) {
        try {
            SiteType existing = siteTypeMapper.selectById(typeId);
            if (existing == null) {
                return Result.error("Site type not found");
            }

            siteType.setTypeId(typeId);
            siteType.setUpdateTime(LocalDateTime.now());
            siteTypeMapper.update(siteType);

            // Log operation
            OperationLog log = new OperationLog(
                    "UPDATE_SITE_TYPE",
                    null,
                    "ADMIN",
                    "Update site type: " + siteType.getTypeName(),
                    "typeId=" + typeId,
                    LocalDateTime.now());
            operationLogMapper.insert(log);

            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Failed to update site type: " + e.getMessage());
        }
    }

    /**
     * Delete site type
     */
    @DeleteMapping("/type/{typeId}")
    public Result<Void> deleteSiteType(@PathVariable Long typeId) {
        try {
            SiteType existing = siteTypeMapper.selectById(typeId);
            if (existing == null) {
                return Result.error("Site type not found");
            }

            List<Site> sites = siteMapper.selectByTypeId(typeId);
            if (!sites.isEmpty()) {
                return Result.error("Cannot delete site type with existing sites");
            }

            siteTypeMapper.delete(typeId);

            // Log operation
            OperationLog log = new OperationLog(
                    "DELETE_SITE_TYPE",
                    null,
                    "ADMIN",
                    "Delete site type: " + existing.getTypeName(),
                    "typeId=" + typeId,
                    LocalDateTime.now());
            operationLogMapper.insert(log);

            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Failed to delete site type: " + e.getMessage());
        }
    }

    // ========== Equipment CRUD ==========

    /**
     * Create equipment
     */
    @PostMapping("/equipment")
    public Result<Object> createEquipment(@RequestBody Equipment equipment) {
        try {
            equipment.setCreateTime(LocalDateTime.now());
            equipment.setUpdateTime(LocalDateTime.now());
            equipmentMapper.insert(equipment);

            // Log operation
            OperationLog log = new OperationLog(
                    "CREATE_EQUIPMENT",
                    null,
                    "ADMIN",
                    "Create equipment: " + equipment.getEquipName(),
                    "equipId=" + equipment.getEquipId(),
                    LocalDateTime.now());
            operationLogMapper.insert(log);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("equipId", equipment.getEquipId());
            result.put("equipName", equipment.getEquipName());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("Failed to create equipment: " + e.getMessage());
        }
    }

    /**
     * Update equipment
     */
    @PutMapping("/equipment/{equipId}")
    public Result<Void> updateEquipment(@PathVariable Long equipId, @RequestBody Equipment equipment) {
        try {
            Equipment existing = equipmentMapper.selectById(equipId);
            if (existing == null) {
                return Result.error("Equipment not found");
            }

            equipment.setEquipId(equipId);
            equipment.setUpdateTime(LocalDateTime.now());
            equipmentMapper.update(equipment);

            // Log operation
            OperationLog log = new OperationLog(
                    "UPDATE_EQUIPMENT",
                    null,
                    "ADMIN",
                    "Update equipment: " + equipment.getEquipName(),
                    "equipId=" + equipId,
                    LocalDateTime.now());
            operationLogMapper.insert(log);

            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Failed to update equipment: " + e.getMessage());
        }
    }

    /**
     * Delete equipment
     */
    @DeleteMapping("/equipment/{equipId}")
    public Result<Void> deleteEquipment(@PathVariable Long equipId) {
        try {
            Equipment existing = equipmentMapper.selectById(equipId);
            if (existing == null) {
                return Result.error("Equipment not found");
            }

            equipmentMapper.delete(equipId);

            // Log operation
            OperationLog log = new OperationLog(
                    "DELETE_EQUIPMENT",
                    null,
                    "ADMIN",
                    "Delete equipment: " + existing.getEquipName(),
                    "equipId=" + equipId,
                    LocalDateTime.now());
            operationLogMapper.insert(log);

            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Failed to delete equipment: " + e.getMessage());
        }
    }
}
