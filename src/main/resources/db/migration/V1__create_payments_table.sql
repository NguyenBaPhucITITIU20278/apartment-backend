CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    description TEXT,
    payment_date TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- Tạo index để tối ưu truy vấn
CREATE INDEX idx_payments_room_id ON payments(room_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_payment_date ON payments(payment_date);

-- Tạo function để tự động cập nhật updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Tạo trigger sử dụng function trên
CREATE TRIGGER update_payments_updated_at
    BEFORE UPDATE
    ON payments
    FOR EACH ROW
    EXECUTE PROCEDURE update_updated_at_column(); 