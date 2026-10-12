CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE tenant (
    id UUID NOT NULL,
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    CONSTRAINT pk_tenant PRIMARY KEY (id),
    CONSTRAINT ck_tenant_name_not_blank CHECK (length(btrim(name)) > 0)
);

CREATE UNIQUE INDEX uq_tenant_name
    ON tenant (lower(btrim(name)));