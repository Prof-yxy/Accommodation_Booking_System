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
('标准帐篷营地', 100.00, 4, '适合家庭露营，包含基础设施', 'https://example.com/standard-tent.jpg', 1),
('豪华帐篷营地', 200.00, 6, '更加舒适的露营体验，配备齐全', 'https://example.com/luxury-tent.jpg', 1),
('RV营地', 150.00, 8, '专为房车停泊设计，水电齐全', 'https://example.com/rv-site.jpg', 1),
('小木屋', 250.00, 4, '舒适的小木屋住宿，有独立浴室', 'https://example.com/cabin.jpg', 1),
('帐篷村落', 80.00, 3, '经济型选择，适合背包客', 'https://example.com/budget-tent.jpg', 1)
ON CONFLICT (type_name) DO NOTHING;

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
('帐篷', 50.00, 100, '住宿', '标准4人帐篷', 1),
('睡袋', 20.00, 150, '睡眠', '三季节睡袋', 1),
('登山包', 30.00, 80, '行李', '60L登山包', 1),
('煤气炉', 25.00, 100, '烹饪', '便携式煤气炉', 1),
('餐具套装', 15.00, 120, '烹饪', '6人用餐具组', 1),
('手电筒', 8.00, 200, '照明', 'LED手电筒', 1),
('防潮垫', 12.00, 100, '睡眠', '自充气防潮垫', 1),
('烧烤架', 40.00, 50, '烹饪', '便携式烧烤架', 1),
('冷却盒', 35.00, 80, '存储', '50L冷却盒', 1),
('急救包', 18.00, 100, '安全', '户外紧急急救包', 1),
('地垫', 5.00, 300, '住宿', '帐篷地垫', 1),
('生火套件', 22.00, 100, '烹饪', '生火工具组', 1)
ON CONFLICT (equip_name) DO NOTHING;

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
