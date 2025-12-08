CREATE VIEW View_Daily_Revenue AS
SELECT date(checkInTime) as date, SUM(totalPrice) as revenue
FROM BookingTable
WHERE status = 1 -- 已支付
GROUP BY date(checkInTime);

-- 需要定义 Function 和 Trigger
CREATE FUNCTION audit_log_func() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO OperationLogTable (operation, operatorID, description, logTime)
    VALUES ('NEW_ORDER', NEW.userID, 'Order ID ' || NEW.bookingID || ' created', NOW());
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER Trigger_Booking_Audit
AFTER INSERT ON BookingTable
FOR EACH ROW EXECUTE FUNCTION audit_log_func();

CREATE INDEX idx_booking_time ON BookingTable (checkInTime, checkOutTime);
CREATE INDEX idx_booking_status ON BookingTable (status);
CREATE INDEX idx_daily_price ON DailyPriceTable (typeID, specificDate);