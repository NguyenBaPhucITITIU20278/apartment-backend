CREATE TABLE momo_transactions (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    request_id VARCHAR(50) NOT NULL,
    order_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    order_info TEXT,
    transaction_id VARCHAR(50),
    response_time TIMESTAMP,
    result_code VARCHAR(10),
    message TEXT,
    pay_type VARCHAR(20),
    signature VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payments(id)
);

-- Tạo index
CREATE INDEX idx_momo_transactions_payment_id ON momo_transactions(payment_id);
CREATE INDEX idx_momo_transactions_order_id ON momo_transactions(order_id);
CREATE INDEX idx_momo_transactions_transaction_id ON momo_transactions(transaction_id);

-- Tạo trigger để tự động cập nhật updated_at
CREATE OR REPLACE FUNCTION update_momo_transactions_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_momo_transactions_updated_at
    BEFORE UPDATE
    ON momo_transactions
    FOR EACH ROW
    EXECUTE PROCEDURE update_momo_transactions_updated_at(); 