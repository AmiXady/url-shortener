
CREATE INDEX idx_links_deleted_at ON links (deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX idx_links_expires_at ON links (expires_at) WHERE expires_at IS NOT NULL;