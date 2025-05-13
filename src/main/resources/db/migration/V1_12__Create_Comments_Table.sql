CREATE TABLE comment (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    apartment_id bigint NOT NULL,
    "user_id" bigint NOT NULL,
    content text NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (apartment_id) REFERENCES apartment(id),
    FOREIGN KEY ("user_id") REFERENCES "user"(id)
); 