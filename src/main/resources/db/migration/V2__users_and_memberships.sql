CREATE TABLE app_user (
    id UUID NOT NULL,
    email TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    CONSTRAINT pk_app_user PRIMARY KEY (id),
    CONSTRAINT ck_app_user_email_not_blank CHECK (length(btrim(email)) > 0),
    CONSTRAINT ck_app_user_password_not_empty CHECK (length(password_hash) > 0)
);

CREATE UNIQUE INDEX uq_app_user_email
    ON app_user (lower(btrim(email)));

CREATE TABLE membership (
    user_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    role TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    CONSTRAINT pk_membership PRIMARY KEY (user_id, tenant_id),
    CONSTRAINT fk_membership_user FOREIGN KEY (user_id) REFERENCES app_user (id)
                        ON DELETE CASCADE,
    CONSTRAINT fk_membership_tenant FOREIGN KEY (tenant_id) REFERENCES tenant (id)
                        ON DELETE CASCADE,
    CONSTRAINT ck_membership_role_is_valid CHECK (role IN ('OWNER', 'STAFF', 'CUSTOMER'))
);

CREATE INDEX idx_membership_by_tenant_id
    ON membership (tenant_id);