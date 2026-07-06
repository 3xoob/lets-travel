CREATE TYPE subscription_status AS ENUM ('PENDING', 'ACTIVE', 'CANCELLED', 'EXPIRED');
CREATE TYPE payment_method     AS ENUM ('STRIPE', 'PAYPAL');
CREATE TYPE payment_status     AS ENUM ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED');

CREATE TABLE payments (
    id                 BIGSERIAL PRIMARY KEY,
    amount             DECIMAL(10,2) NOT NULL,
    currency           VARCHAR(3) NOT NULL DEFAULT 'USD',
    method             payment_method NOT NULL,
    provider_ref       VARCHAR(255),
    provider_intent_id VARCHAR(255),
    status             payment_status NOT NULL DEFAULT 'PENDING',
    paid_at            TIMESTAMP,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE subscriptions (
    id                  BIGSERIAL PRIMARY KEY,
    travel_id           BIGINT NOT NULL REFERENCES travels(id),
    traveler_id         BIGINT NOT NULL REFERENCES users(id),
    payment_id          BIGINT REFERENCES payments(id),
    status              subscription_status NOT NULL DEFAULT 'PENDING',
    subscribed_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    cancelled_at        TIMESTAMP,
    cancellation_reason TEXT,
    CONSTRAINT uq_subscription UNIQUE (travel_id, traveler_id)
);

CREATE INDEX idx_subscriptions_travel    ON subscriptions(travel_id);
CREATE INDEX idx_subscriptions_traveler  ON subscriptions(traveler_id);
CREATE INDEX idx_subscriptions_status    ON subscriptions(status);
