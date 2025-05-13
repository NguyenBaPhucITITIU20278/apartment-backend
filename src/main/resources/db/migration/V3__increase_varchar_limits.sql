-- Increase VARCHAR limits for existing columns
ALTER TABLE payments 
    ALTER COLUMN payment_method TYPE VARCHAR(50),
    ALTER COLUMN status TYPE VARCHAR(50),
    ALTER COLUMN transaction_id TYPE VARCHAR(255),
    ALTER COLUMN payment_type TYPE VARCHAR(50),
    ALTER COLUMN package_info TYPE VARCHAR(50); 