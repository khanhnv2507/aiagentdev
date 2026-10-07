SELECT version();
SELECT current_database();

-- agents chỉ đại diện cho Agent là ai, không chứa prompt/responsibility cụ thể.
CREATE TABLE agents (
    id BIGSERIAL PRIMARY KEY,

    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

----------------ví dụ------------------
-- id | code       | name
-- ---|------------|-------------------
-- 1  | BA         | Business Analyst
-- 2  | TECH_LEAD  | Tech Lead
-- 3  | BACKEND    | Backend Developer
-- 4  | FRONTEND   | Frontend Developer
----------------------------------------

CREATE TABLE agent_versions (
    id BIGSERIAL PRIMARY KEY,

    agent_id BIGINT NOT NULL,

    version INTEGER NOT NULL,

    system_instruction TEXT NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    model_provider VARCHAR(50),
    model_name VARCHAR(100),

    temperature NUMERIC(3,2),
    max_tokens INTEGER,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_agent_versions_agent
        FOREIGN KEY (agent_id)
        REFERENCES agents(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_agent_version
        UNIQUE (agent_id, version),

    CONSTRAINT chk_agent_version_status
        CHECK (
            status IN (
                'DRAFT',
                'ACTIVE',
                'INACTIVE'
            )
        )
);

-- Cho phép chỉnh BA mà không phá history cũ.
-- BA
-- │
-- ├── v1
-- │   status = INACTIVE
-- │
-- ├── v2
-- │   status = ACTIVE
-- │
-- └── v3
--     status = DRAFT

----------------------------------------
CREATE TABLE agent_responsibilities (
    id BIGSERIAL PRIMARY KEY,

    agent_version_id BIGINT NOT NULL,

    responsibility_type VARCHAR(30) NOT NULL,

    content TEXT NOT NULL,

    priority INTEGER NOT NULL DEFAULT 0,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_responsibility_agent_version
        FOREIGN KEY (agent_version_id)
        REFERENCES agent_versions(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_responsibility_type
        CHECK (
            responsibility_type IN (
                'MUST_DO',
                'CAN_DO',
                'MUST_NOT_DO'
            )
        )
);

-- Không nhét responsibilities thành một đoạn text lớn.
-- Mỗi responsibility là một record.
-------------------------------------------------------

CREATE TABLE agent_contracts (
    id BIGSERIAL PRIMARY KEY,

    agent_version_id BIGINT NOT NULL,

    contract_type VARCHAR(30) NOT NULL,

    name VARCHAR(100) NOT NULL,

    content JSONB NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_contract_agent_version
        FOREIGN KEY (agent_version_id)
        REFERENCES agent_versions(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_contract_type
        CHECK (
            contract_type IN (
                'INPUT',
                'OUTPUT',
                'CONSTRAINT',
                'COMPLETION'
            )
        )
);
-- Contract định nghĩa Agent phải hoạt động theo luật nào.
------------------------------------------------------------


-- agents
-- │
-- │ 1:N
-- ▼
-- agent_versions
-- │
-- ├───────────────┐
-- │ 1:N           │ 1:N
-- ▼               ▼
-- agent_          agent_

------------------------------------------------------------




