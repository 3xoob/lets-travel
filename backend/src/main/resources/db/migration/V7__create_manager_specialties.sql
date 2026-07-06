CREATE TABLE IF NOT EXISTS manager_specialties (
    manager_profile_id BIGINT NOT NULL REFERENCES manager_profiles(id) ON DELETE CASCADE,
    specialty          VARCHAR(255) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_manager_specialties_profile ON manager_specialties(manager_profile_id);
