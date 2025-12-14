-- ============================================================================
-- 初始数据插入
-- ============================================================================

-- 1. 插入测试用户
INSERT INTO users (username, password, phone, role, create_time, update_time) VALUES
('admin', '$2a$10$oY0gZp0kpcLUPlXfX82HUOTXCiAWML293v8bnApCQYMOyoKPEqL6y', '1145141919810', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user1', '$2a$10$Rb4VDI0w7xmSWwIRkuDiBuUML25NToW0Pgf1.NTlZq47LCYltRW8C', '12345678901', 'user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- 2. 插入营地类型
INSERT INTO site_types (type_name, base_price, max_guests, description, image_url, status) VALUES
('湖景標准營位', 120.00, 4, '臨湖草地，含電桩與野餐桌', 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=800&q=80', 1),
('森林豪華營位', 220.00, 6, '寬闊樹蔭，含私密遮陽與吊床', 'https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=800&q=80', 1),
('星空A字小屋', 320.00, 4, '硬頂小屋，配備空調與獨立衛浴', 'https://images.unsplash.com/photo-1469474968028-56623f02e42e?auto=format&fit=crop&w=800&q=80', 1),
('全套接駁房車位', 180.00, 8, '車位自帶上下水與30A電源', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=800&q=80', 1),
('輕奢鈴鐺帳', 260.00, 4, '木平台 + 大空間棉布帳，含氛圍燈', 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=800&q=80', 1)
ON CONFLICT (type_name) DO UPDATE SET
    base_price = EXCLUDED.base_price,
    max_guests = EXCLUDED.max_guests,
    description = EXCLUDED.description,
    image_url = EXCLUDED.image_url,
    status = EXCLUDED.status;

-- 3. 插入营地
INSERT INTO sites (type_id, site_no, status, create_time, update_time) 
SELECT t.type_id, 'Site-' || t.type_id || '-' || seq.num, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM site_types t
CROSS JOIN (SELECT generate_series(1, 10) AS num) seq
WHERE NOT EXISTS (
    SELECT 1 FROM sites s WHERE s.type_id = t.type_id
);

-- 4. 插入设备
INSERT INTO equipments (equip_name, unit_price, total_stock, category, description, status) VALUES
('羽絨睡袋', 28.00, 120, '睡眠', '舒適溫標5C，可壓縮', 1),
('自充氣防潮墊', 16.00, 160, '睡眠', '5cm 厚度，R值 3.5', 1),
('鈦合金炊煮套裝', 45.00, 90, '烹飪', '含鍋碗與酒精爐架', 1),
('雙口瓦斯爐', 55.00, 70, '烹飪', '含兩罐230g氣罐', 1),
('可折疊桌椅組', 32.00, 140, '營地', '四人桌 + 四折疊椅', 1),
('LED氛圍燈串', 12.00, 180, '照明', 'USB 供電，10m 長', 1),
('便攜保溫冰箱', 48.00, 80, '存儲', '42L，附車載電源線', 1),
('戶外咖啡組', 26.00, 110, '烹飪', '手沖壺 + 濾杯 + 豆', 1)
ON CONFLICT (equip_name) DO UPDATE SET
    unit_price = EXCLUDED.unit_price,
    total_stock = EXCLUDED.total_stock,
    category = EXCLUDED.category,
    description = EXCLUDED.description,
    status = EXCLUDED.status;

-- 5. 插入每日价格（未来60天的动态价格）
INSERT INTO daily_prices (type_id, specific_date, price, remark, create_time)
SELECT 
    t.type_id,
    CURRENT_DATE + seq.day,
    CASE 
        WHEN EXTRACT(DOW FROM CURRENT_DATE + seq.day) IN (5, 6) THEN -- 周五、周六上浮20%
            (t.base_price * 1.2)
        WHEN EXTRACT(DOW FROM CURRENT_DATE + seq.day) = 0 THEN -- 周日上浮10%
            (t.base_price * 1.1)
        ELSE
            t.base_price
    END,
    CASE 
        WHEN EXTRACT(DOW FROM CURRENT_DATE + seq.day) IN (5, 6) THEN '周末价格'
        WHEN EXTRACT(DOW FROM CURRENT_DATE + seq.day) = 0 THEN '周日价格'
        ELSE '平日价格'
    END,
    CURRENT_TIMESTAMP
FROM site_types t
CROSS JOIN (SELECT generate_series(1, 90) AS day) seq
WHERE NOT EXISTS (
    SELECT 1 FROM daily_prices dp 
    WHERE dp.type_id = t.type_id 
    AND dp.specific_date = CURRENT_DATE + seq.day
)
AND t.status = 1;

-- 6. 插入一些示例预订
INSERT INTO bookings (user_id, site_id, type_id, check_in, check_out, guest_name, guest_phone, total_price, status, create_time, update_time)
SELECT 
    u.user_id,
    s.site_id,
    s.type_id,
    CURRENT_DATE + 3,
    CURRENT_DATE + 5,
    u.username || ' Family',
    u.phone,
    (t.base_price * 2),
    1, -- 已支付
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u
JOIN sites s ON s.type_id = 1
JOIN site_types t ON t.type_id = s.type_id
WHERE u.username IN ('user1', 'user2')
AND s.site_id <= 2
AND NOT EXISTS (
    SELECT 1 FROM bookings b 
    WHERE b.user_id = u.user_id 
    AND b.check_in = CURRENT_DATE + 3
);

-- 7. 为示例预订添加设备
INSERT INTO booking_equips (booking_id, equip_id, quantity)
SELECT 
    b.booking_id,
    e.equip_id,
    CASE 
        WHEN e.equip_id = 1 THEN 1 -- 帐篷 1个
        WHEN e.equip_id = 2 THEN 4 -- 睡袋 4个
        WHEN e.equip_id = 3 THEN 1 -- 登山包 1个
        WHEN e.equip_id = 4 THEN 1 -- 煤气炉 1个
        WHEN e.equip_id = 5 THEN 1 -- 餐具套装 1个
        WHEN e.equip_id = 6 THEN 2 -- 手电筒 2个
        ELSE 0
    END
FROM bookings b
CROSS JOIN equipments e
WHERE b.status = 1
AND e.equip_id IN (1, 2, 3, 4, 5, 6)
AND NOT EXISTS (
    SELECT 1 FROM booking_equips be
    WHERE be.booking_id = b.booking_id
    AND be.equip_id = e.equip_id
);

-- 8. 记录初始化日志
INSERT INTO operation_logs (operation, operator_id, operator_name, description, details, log_time) VALUES
('system_init', NULL, 'system', 'Database initialized', 'Initial data setup completed', CURRENT_TIMESTAMP);

-- 9. 输出统计信息
SELECT '用户总数' as stat_name, COUNT(*) as count FROM users
UNION ALL
SELECT '营地类型数', COUNT(*) FROM site_types
UNION ALL
SELECT '营地总数', COUNT(*) FROM sites
UNION ALL
SELECT '设备类型数', COUNT(*) FROM equipments
UNION ALL
SELECT '预订数', COUNT(*) FROM bookings
UNION ALL
SELECT '预订项目数', COUNT(*) FROM booking_equips;
