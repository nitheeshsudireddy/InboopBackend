-- V13: Create workspace_plans table for formal plan management
-- This table tracks subscription/plan status for each workspace

CREATE TABLE IF NOT EXISTS workspace_plans (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL UNIQUE REFERENCES workspaces(id) ON DELETE CASCADE,
    plan VARCHAR(50) NOT NULL DEFAULT 'FREE',
    plan_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for quick lookups by workspace
CREATE INDEX IF NOT EXISTS idx_workspace_plans_workspace_id ON workspace_plans(workspace_id);

-- Index for finding expired plans
CREATE INDEX IF NOT EXISTS idx_workspace_plans_expires_at ON workspace_plans(expires_at) WHERE expires_at IS NOT NULL;

-- Migrate existing workspaces to have plan records
-- All existing workspaces get PRO plan with ACTIVE status
INSERT INTO workspace_plans (workspace_id, plan, plan_status, started_at)
SELECT id, plan, 'ACTIVE', created_at
FROM workspaces
WHERE deleted_at IS NULL
ON CONFLICT (workspace_id) DO NOTHING;
