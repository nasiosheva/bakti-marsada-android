CREATE TABLE IF NOT EXISTS events (
    id TEXT PRIMARY KEY,
    tenant_id TEXT NOT NULL,
    sector_id TEXT NOT NULL,
    sector_name TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    scheduled_at TEXT NOT NULL,
    location TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS event_program_items (
    id TEXT PRIMARY KEY,
    event_id TEXT NOT NULL,
    order_index INTEGER NOT NULL,
    title TEXT NOT NULL DEFAULT '',
    content TEXT NOT NULL DEFAULT '',
    leader TEXT NOT NULL DEFAULT '',
    type TEXT NOT NULL DEFAULT 'CUSTOM',
    scripture_reference TEXT NOT NULL DEFAULT '',
    scripture_text TEXT NOT NULL DEFAULT '',
    note TEXT NOT NULL DEFAULT '',
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS worship_templates (
    id TEXT PRIMARY KEY,
    tenant_id TEXT NOT NULL,
    sector_id TEXT,
    title TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS worship_template_items (
    id TEXT PRIMARY KEY,
    template_id TEXT NOT NULL,
    order_index INTEGER NOT NULL,
    title TEXT NOT NULL DEFAULT '',
    content TEXT NOT NULL DEFAULT '',
    leader TEXT NOT NULL DEFAULT '',
    type TEXT NOT NULL DEFAULT 'CUSTOM',
    scripture_reference TEXT NOT NULL DEFAULT '',
    scripture_text TEXT NOT NULL DEFAULT '',
    note TEXT NOT NULL DEFAULT '',
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES worship_templates(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS members (
    id TEXT PRIMARY KEY,
    tenant_id TEXT NOT NULL,
    sector_id TEXT NOT NULL,
    sector_name TEXT NOT NULL,
    full_name TEXT NOT NULL,
    family_group TEXT NOT NULL DEFAULT '',
    phone_number TEXT NOT NULL DEFAULT '',
    address TEXT NOT NULL DEFAULT '',
    role_in_sector TEXT NOT NULL DEFAULT '',
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS finance_reports (
    id TEXT PRIMARY KEY,
    tenant_id TEXT NOT NULL,
    sector_id TEXT NOT NULL,
    sector_name TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    period_label TEXT NOT NULL,
    amount INTEGER NOT NULL DEFAULT 0,
    is_visible_to_jemaat INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payment_obligations (
    id TEXT PRIMARY KEY,
    tenant_id TEXT NOT NULL,
    sector_id TEXT NOT NULL,
    sector_name TEXT NOT NULL,
    member_id TEXT NOT NULL,
    member_name TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    amount INTEGER NOT NULL DEFAULT 0,
    due_date TEXT NOT NULL,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_events_tenant_sector ON events(tenant_id, sector_id);
CREATE INDEX IF NOT EXISTS idx_events_schedule ON events(scheduled_at);

CREATE INDEX IF NOT EXISTS idx_event_program_items_event ON event_program_items(event_id);
CREATE INDEX IF NOT EXISTS idx_event_program_items_order ON event_program_items(event_id, order_index);

CREATE INDEX IF NOT EXISTS idx_worship_templates_tenant_sector ON worship_templates(tenant_id, sector_id);
CREATE INDEX IF NOT EXISTS idx_worship_template_items_template ON worship_template_items(template_id);
CREATE INDEX IF NOT EXISTS idx_worship_template_items_order ON worship_template_items(template_id, order_index);

CREATE INDEX IF NOT EXISTS idx_members_tenant_sector ON members(tenant_id, sector_id);
CREATE INDEX IF NOT EXISTS idx_members_name ON members(full_name);

CREATE INDEX IF NOT EXISTS idx_finance_reports_tenant_sector ON finance_reports(tenant_id, sector_id);
CREATE INDEX IF NOT EXISTS idx_finance_reports_visibility ON finance_reports(is_visible_to_jemaat);

CREATE INDEX IF NOT EXISTS idx_payment_obligations_tenant_sector ON payment_obligations(tenant_id, sector_id);
CREATE INDEX IF NOT EXISTS idx_payment_obligations_member ON payment_obligations(member_id);
CREATE INDEX IF NOT EXISTS idx_payment_obligations_status ON payment_obligations(status);
