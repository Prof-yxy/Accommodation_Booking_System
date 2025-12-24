package com.camping.config;

import com.camping.entity.Equipment;
import com.camping.entity.Site;
import com.camping.entity.SiteType;
import com.camping.mapper.EquipmentMapper;
import com.camping.mapper.SiteMapper;
import com.camping.mapper.SiteTypeMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeedRunner implements ApplicationRunner {

    private final SiteTypeMapper siteTypeMapper;
    private final SiteMapper siteMapper;
    private final EquipmentMapper equipmentMapper;

    public DataSeedRunner(SiteTypeMapper siteTypeMapper, SiteMapper siteMapper, EquipmentMapper equipmentMapper) {
        this.siteTypeMapper = siteTypeMapper;
        this.siteMapper = siteMapper;
        this.equipmentMapper = equipmentMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedSiteTypesAndSitesIfEmpty();
        seedEquipmentsIfEmpty();
    }

    private void seedSiteTypesAndSitesIfEmpty() {
        List<SiteType> existingTypes = safeList(siteTypeMapper.selectAll());
        if (existingTypes.isEmpty()) {
            insertSiteType("湖景標准營位", new BigDecimal("120.00"), 4,
                    "臨湖草地，含電桩與野餐桌",
                    "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=800&q=80");
            insertSiteType("森林豪華營位", new BigDecimal("220.00"), 6,
                    "寬闊樹蔭，含私密遮陽與吊床",
                    "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=800&q=80");
            insertSiteType("星空A字小屋", new BigDecimal("320.00"), 4,
                    "硬頂小屋，配備空調與獨立衛浴",
                    "https://images.unsplash.com/photo-1469474968028-56623f02e42e?auto=format&fit=crop&w=800&q=80");
            insertSiteType("全套接駁房車位", new BigDecimal("180.00"), 8,
                    "車位自帶上下水與30A電源",
                    "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=800&q=80");
            insertSiteType("輕奢鈴鐺帳", new BigDecimal("260.00"), 4,
                    "木平台 + 大空間棉布帳，含氛圍燈",
                    "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=800&q=80");

            existingTypes = safeList(siteTypeMapper.selectAll());
        }

        for (SiteType type : existingTypes) {
            List<Site> sites = safeList(siteMapper.selectByTypeId(type.getTypeId()));
            if (!sites.isEmpty()) {
                continue;
            }
            for (int i = 1; i <= 10; i++) {
                Site site = new Site();
                site.setTypeId(type.getTypeId());
                site.setSiteNo("Site-" + type.getTypeId() + "-" + i);
                site.setStatus(1);
                site.setCreateTime(LocalDateTime.now());
                site.setUpdateTime(LocalDateTime.now());
                siteMapper.insert(site);
            }
        }
    }

    private void seedEquipmentsIfEmpty() {
        List<Equipment> existing = safeList(equipmentMapper.selectAll());
        if (!existing.isEmpty()) {
            return;
        }

        insertEquipment("羽絨睡袋", new BigDecimal("28.00"), 120, "睡眠", "舒適溫標5C，可壓縮");
        insertEquipment("自充氣防潮墊", new BigDecimal("16.00"), 160, "睡眠", "5cm 厚度，R值 3.5");
        insertEquipment("鈦合金炊煮套裝", new BigDecimal("45.00"), 90, "烹飪", "含鍋碗與酒精爐架");
        insertEquipment("雙口瓦斯爐", new BigDecimal("55.00"), 70, "烹飪", "含兩罐230g氣罐");
        insertEquipment("可折疊桌椅組", new BigDecimal("32.00"), 140, "營地", "四人桌 + 四折疊椅");
        insertEquipment("LED氛圍燈串", new BigDecimal("12.00"), 180, "照明", "USB 供電，10m 長");
        insertEquipment("便攜保溫冰箱", new BigDecimal("48.00"), 80, "存儲", "42L，附車載電源線");
        insertEquipment("戶外咖啡組", new BigDecimal("26.00"), 110, "烹飪", "手沖壺 + 濾杯 + 豆");
    }

    private void insertSiteType(String name, BigDecimal basePrice, int maxGuests, String desc, String imageUrl) {
        SiteType type = new SiteType();
        type.setTypeName(name);
        type.setBasePrice(basePrice);
        type.setMaxGuests(maxGuests);
        type.setDescription(desc);
        type.setImageUrl(imageUrl);
        type.setStatus(1);
        siteTypeMapper.insert(type);
    }

    private void insertEquipment(String name, BigDecimal unitPrice, int totalStock, String category, String desc) {
        Equipment equipment = new Equipment();
        equipment.setEquipName(name);
        equipment.setUnitPrice(unitPrice);
        equipment.setTotalStock(totalStock);
        equipment.setCategory(category);
        equipment.setDescription(desc);
        equipment.setStatus(1);
        equipmentMapper.insert(equipment);
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? java.util.Collections.emptyList() : list;
    }
}
