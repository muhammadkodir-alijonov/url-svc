-- Initial schema for URL Shortener
-- Flyway migration V1

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    keycloak_id VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    plan VARCHAR(50) NOT NULL DEFAULT 'FREE',
    links_created INTEGER NOT NULL DEFAULT 0,
    links_limit INTEGER NOT NULL DEFAULT 100,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for fast lookup
CREATE INDEX IF NOT EXISTS idx_users_keycloak_id ON users(keycloak_id);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- URLs table
CREATE TABLE IF NOT EXISTS urls (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    short_code VARCHAR(10) UNIQUE NOT NULL,
    original_url TEXT NOT NULL,
    title VARCHAR(500),
    description TEXT,
    clicks BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_urls_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_urls_short_code ON urls(short_code);
CREATE INDEX IF NOT EXISTS idx_urls_user_id ON urls(user_id);
CREATE INDEX IF NOT EXISTS idx_urls_active ON urls(is_active);
CREATE INDEX IF NOT EXISTS idx_urls_expires_at ON urls(expires_at);

-- URL clicks tracking table (optional - for analytics)
CREATE TABLE IF NOT EXISTS url_clicks (
    id UUID PRIMARY KEY,
    url_id UUID NOT NULL,
    clicked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    referer TEXT,
    country VARCHAR(2),
    city VARCHAR(100),
    device_type VARCHAR(50),
    CONSTRAINT fk_clicks_url FOREIGN KEY (url_id) REFERENCES urls(id) ON DELETE CASCADE
);

-- Index for analytics queries
CREATE INDEX IF NOT EXISTS idx_clicks_url_id ON url_clicks(url_id);
CREATE INDEX IF NOT EXISTS idx_clicks_clicked_at ON url_clicks(clicked_at);

-- Comments for documentation
COMMENT ON TABLE users IS 'User accounts synchronized from Keycloak';
COMMENT ON TABLE urls IS 'Shortened URLs created by users';
COMMENT ON TABLE url_clicks IS 'Click tracking and analytics data';

COMMENT ON COLUMN users.keycloak_id IS 'User ID from Keycloak (sub claim)';
COMMENT ON COLUMN users.plan IS 'User subscription plan: FREE, PRO, ENTERPRISE';
COMMENT ON COLUMN urls.short_code IS 'Unique short code for the URL (e.g., abc123)';
COMMENT ON COLUMN urls.clicks IS 'Total number of clicks (denormalized for performance)';


-- function for updating updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

-- trigger for updating updated_at column on urls and users tables
CREATE TRIGGER update_urls_updated_at
    BEFORE UPDATE ON urls
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Grant permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO admin;