CREATE TYPE travel_category AS ENUM (
    'ADVENTURE', 'CULTURAL', 'BEACH', 'BUSINESS', 'ECO',
    'WELLNESS', 'FAMILY', 'ROMANTIC', 'CITY_BREAK', 'CRUISE'
);
CREATE TYPE travel_status AS ENUM ('DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED');

CREATE TABLE travels (
    id                    BIGSERIAL PRIMARY KEY,
    title                 VARCHAR(255) NOT NULL,
    description           TEXT NOT NULL,
    destination_city      VARCHAR(100) NOT NULL,
    destination_country   VARCHAR(100) NOT NULL,
    destination_latitude  DECIMAL(9,6),
    destination_longitude DECIMAL(9,6),
    category              travel_category NOT NULL,
    tags                  TEXT[],
    start_date            DATE NOT NULL,
    end_date              DATE NOT NULL,
    price                 DECIMAL(10,2) NOT NULL,
    capacity              INTEGER NOT NULL,
    current_enrollment    INTEGER NOT NULL DEFAULT 0,
    image_urls            TEXT[],
    status                travel_status NOT NULL DEFAULT 'DRAFT',
    manager_id            BIGINT NOT NULL REFERENCES users(id),
    version               BIGINT NOT NULL DEFAULT 0,
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_travels_manager ON travels(manager_id);
CREATE INDEX idx_travels_status ON travels(status);
CREATE INDEX idx_travels_category ON travels(category);
CREATE INDEX idx_travels_start_date ON travels(start_date);
