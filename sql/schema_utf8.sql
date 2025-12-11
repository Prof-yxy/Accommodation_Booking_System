-- Accommodation Booking System - Database Schema (UTF-8 Safe)
-- Character encoding: UTF-8 (no special characters, ASCII-safe)

-- User table
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'user',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Site types table
CREATE TABLE IF NOT EXISTS site_types (
    type_id BIGSERIAL PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL,
    max_guests INTEGER NOT NULL,
    description TEXT,
    image_url VARCHAR(255),
    status INTEGER DEFAULT 1
);

-- Sites table
CREATE TABLE IF NOT EXISTS sites (
    site_id BIGSERIAL PRIMARY KEY,
    type_id BIGINT NOT NULL,
    site_no VARCHAR(50) NOT NULL,
    status INTEGER DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (type_id) REFERENCES site_types(type_id),
    UNIQUE(type_id, site_no)
);

-- Bookings table
CREATE TABLE IF NOT EXISTS bookings (
    booking_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    type_id BIGINT NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    guest_name VARCHAR(100) NOT NULL,
    guest_phone VARCHAR(20) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (site_id) REFERENCES sites(site_id),
    FOREIGN KEY (type_id) REFERENCES site_types(type_id)
);

-- Equipment table
CREATE TABLE IF NOT EXISTS equipments (
    equip_id BIGSERIAL PRIMARY KEY,
    equip_name VARCHAR(100) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_stock INTEGER NOT NULL,
    category VARCHAR(50),
    description TEXT,
    status INTEGER DEFAULT 1 -- 1: 可用, 0: 停用
);

-- Daily prices table
CREATE TABLE IF NOT EXISTS daily_prices (
    price_id BIGSERIAL PRIMARY KEY,
    type_id BIGINT NOT NULL,
    specific_date DATE NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    remark VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (type_id) REFERENCES site_types(type_id),
    UNIQUE(type_id, specific_date)
);

-- Booking equipment table
CREATE TABLE IF NOT EXISTS booking_equips (
    booking_equip_id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    equip_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    FOREIGN KEY (equip_id) REFERENCES equipments(equip_id)
);

-- 创建操作日志表
CREATE TABLE IF NOT EXISTS operation_logs (
    log_id BIGSERIAL PRIMARY KEY,
    operation VARCHAR(50),
    operator_id BIGINT,
    operator_name VARCHAR(100),
    description TEXT,
    details TEXT,
    log_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引提高查询性能
CREATE INDEX IF NOT EXISTS idx_bookings_user_id ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_type_id ON bookings(type_id);
CREATE INDEX IF NOT EXISTS idx_bookings_site_id ON bookings(site_id);
CREATE INDEX IF NOT EXISTS idx_bookings_check_in_out ON bookings(check_in, check_out);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_sites_type_id ON sites(type_id);
CREATE INDEX IF NOT EXISTS idx_daily_prices_type_date ON daily_prices(type_id, specific_date);
CREATE INDEX IF NOT EXISTS idx_booking_equips_booking_id ON booking_equips(booking_id);
CREATE INDEX IF NOT EXISTS idx_booking_equips_equip_id ON booking_equips(equip_id);
CREATE INDEX IF NOT EXISTS idx_operation_logs_log_time ON operation_logs(log_time);

-- 创建日收入视图
CREATE OR REPLACE VIEW view_daily_revenue AS
SELECT 
    DATE(b.create_time) as revenue_date,
    st.type_id,
    st.type_name,
    COUNT(DISTINCT b.booking_id) as booking_count,
    SUM(CASE WHEN b.status = 1 THEN 1 ELSE 0 END) as paid_count,
    SUM(CASE WHEN b.status = 1 THEN b.total_price ELSE 0 END) as revenue,
    SUM(CASE WHEN b.status = 0 THEN b.total_price ELSE 0 END) as pending_amount
FROM bookings b
JOIN site_types st ON b.type_id = st.type_id
GROUP BY DATE(b.create_time), st.type_id, st.type_name
ORDER BY revenue_date DESC;

-- 创建预订触发器用于操作日志
CREATE OR REPLACE FUNCTION log_booking_operation()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO operation_logs (operation, operator_id, operator_name, description, details, log_time)
        VALUES ('create_booking', NEW.user_id, 'user', 'Booking created', 
                JSON_BUILD_OBJECT('booking_id', NEW.booking_id, 'type_id', NEW.type_id, 'check_in', NEW.check_in, 'check_out', NEW.check_out)::text, 
                CURRENT_TIMESTAMP);
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.status != NEW.status THEN
            INSERT INTO operation_logs (operation, operator_id, operator_name, description, details, log_time)
            VALUES ('update_booking_status', NEW.user_id, 'user', 'Booking status updated', 
                    JSON_BUILD_OBJECT('booking_id', NEW.booking_id, 'old_status', OLD.status, 'new_status', NEW.status)::text, 
                    CURRENT_TIMESTAMP);
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_booking_log
AFTER INSERT OR UPDATE ON bookings
FOR EACH ROW
EXECUTE FUNCTION log_booking_operation();
