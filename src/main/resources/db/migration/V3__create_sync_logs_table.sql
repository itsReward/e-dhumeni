-- V3__create_sync_logs_table.sql
CREATE TABLE sync_logs (
    id UUID PRIMARY KEY,
    entity_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    synced_by VARCHAR(100) NOT NULL,
    message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Create an index on entity_id for faster lookups
CREATE INDEX idx_sync_logs_entity_id ON sync_logs(entity_id);

-- Create an index on the combination of entity_type and status for filtering
CREATE INDEX idx_sync_logs_type_status ON sync_logs(entity_type, status);

-- Create an index on created_at for timeline queries
CREATE INDEX idx_sync_logs_created_at ON sync_logs(created_at);

-- Add a comment to the table
COMMENT ON TABLE sync_logs IS 'Stores synchronization logs for entities shared with external systems';

-- Add comments to columns
COMMENT ON COLUMN sync_logs.id IS 'Primary key';
COMMENT ON COLUMN sync_logs.entity_id IS 'UUID of the entity being synchronized';
COMMENT ON COLUMN sync_logs.entity_type IS 'Type of entity being synchronized (e.g., Farmer, Contract)';
COMMENT ON COLUMN sync_logs.status IS 'Status of synchronization (SUCCESS, FAILED, PENDING)';
COMMENT ON COLUMN sync_logs.synced_by IS 'User or system that initiated the sync';
COMMENT ON COLUMN sync_logs.message IS 'Optional message with details about the sync operation';
COMMENT ON COLUMN sync_logs.created_at IS 'Timestamp when the sync log was created';