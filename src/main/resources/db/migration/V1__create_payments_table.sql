CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT,
    amount DECIMAL(15,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    description TEXT,
    payment_date TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255),
    payment_type VARCHAR(50),
    package_info VARCHAR(50),
    duration INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
);

-- Create indexes for better query performance
CREATE INDEX idx_payments_room_id ON payments(room_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_payment_date ON payments(payment_date);
CREATE INDEX idx_payments_payment_type ON payments(payment_type);

-- Create function to automatically update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger using the function
CREATE TRIGGER update_payments_updated_at
    BEFORE UPDATE
    ON payments
    FOR EACH ROW
    EXECUTE PROCEDURE update_updated_at_column(); 