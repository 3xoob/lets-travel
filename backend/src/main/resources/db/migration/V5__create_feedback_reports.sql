CREATE TYPE report_target_type AS ENUM ('MANAGER', 'TRAVELER', 'TRAVEL');
CREATE TYPE report_status      AS ENUM ('OPEN', 'RESOLVED', 'DISMISSED');

CREATE TABLE feedback (
    id          BIGSERIAL PRIMARY KEY,
    travel_id   BIGINT NOT NULL REFERENCES travels(id),
    traveler_id BIGINT NOT NULL REFERENCES users(id),
    rating      INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_feedback UNIQUE (travel_id, traveler_id)
);

CREATE INDEX idx_feedback_travel   ON feedback(travel_id);
CREATE INDEX idx_feedback_traveler ON feedback(traveler_id);

CREATE TABLE reports (
    id          BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL REFERENCES users(id),
    target_type report_target_type NOT NULL,
    target_id   BIGINT NOT NULL,
    reason      VARCHAR(100) NOT NULL,
    detail      TEXT,
    status      report_status NOT NULL DEFAULT 'OPEN',
    reviewed_by BIGINT REFERENCES users(id),
    reviewed_at TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reports_reporter ON reports(reporter_id);
CREATE INDEX idx_reports_status   ON reports(status);
