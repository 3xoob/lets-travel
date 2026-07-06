CREATE TABLE IF NOT EXISTS travel_tags (
    travel_id BIGINT NOT NULL REFERENCES travels(id) ON DELETE CASCADE,
    tag       VARCHAR(100) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_travel_tags_travel ON travel_tags(travel_id);

CREATE TABLE IF NOT EXISTS travel_images (
    travel_id  BIGINT NOT NULL REFERENCES travels(id) ON DELETE CASCADE,
    image_url  VARCHAR(500) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_travel_images_travel ON travel_images(travel_id);
