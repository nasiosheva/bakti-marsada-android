CREATE TABLE IF NOT EXISTS arisan_participants (
    tenant_id TEXT NOT NULL,
    sector_id TEXT NOT NULL,
    member_id TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, sector_id, member_id),
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_arisan_participants_tenant_sector
    ON arisan_participants(tenant_id, sector_id);

CREATE INDEX IF NOT EXISTS idx_arisan_participants_member
    ON arisan_participants(member_id);
