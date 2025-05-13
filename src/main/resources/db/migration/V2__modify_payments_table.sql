-- Drop the existing foreign key constraint
ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_room_id_fkey;

-- Add new foreign key constraint with ON DELETE SET NULL
ALTER TABLE payments 
    ADD CONSTRAINT payments_room_id_fkey 
    FOREIGN KEY (room_id) 
    REFERENCES rooms(id) 
    ON DELETE SET NULL;

-- Add new columns for listing payments
ALTER TABLE payments 
    ADD COLUMN IF NOT EXISTS payment_type VARCHAR(20),
    ADD COLUMN IF NOT EXISTS package_info VARCHAR(20),
    ADD COLUMN IF NOT EXISTS duration INTEGER;

-- Update existing constraint to allow null
ALTER TABLE payments ALTER COLUMN room_id DROP NOT NULL; 