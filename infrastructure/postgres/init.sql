CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
--
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       keycloak_id VARCHAR(100) UNIQUE NOT NULL,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       plan VARCHAR(20) DEFAULT 'FREE' CHECK (plan IN ('FREE', 'PRO')),
                       links_created INT DEFAULT 0 CHECK (links_created >= 0),
                       links_limit INT DEFAULT 100,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--
CREATE TABLE urls (
                      id BIGSERIAL PRIMARY KEY,
                      short_code VARCHAR(10) UNIQUE NOT NULL,
                      original_url TEXT NOT NULL,
                      user_id UUID REFERENCES users(id) ON DELETE CASCADE,

    -- Security
                      password_hash VARCHAR(255),

    -- Metadata
                      title VARCHAR(255),

    -- Analytics
                      clicks INT DEFAULT 0 CHECK (clicks >= 0),

    -- Expiration
                      expires_at TIMESTAMP,

    -- Status
                      is_active BOOLEAN DEFAULT true NOT NULL,
                      is_custom BOOLEAN DEFAULT false NOT NULL,

    -- Timestamps
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                      last_accessed_at TIMESTAMP,

    -- Constraints
                      CONSTRAINT urls_check_short_code_length
                          CHECK (length(short_code) >= 4 AND length(short_code) <= 10),
                      CONSTRAINT urls_check_original_url_not_empty
                          CHECK (length(original_url) > 0)
);
--  index urls
CREATE INDEX idx_urls_short_code ON urls(short_code);
CREATE INDEX idx_urls_user_id ON urls(user_id);
CREATE INDEX idx_urls_created_at ON urls(created_at DESC);
CREATE INDEX idx_urls_active ON urls(is_active) WHERE is_active = true;
CREATE INDEX idx_urls_expires_at ON urls(expires_at) WHERE expires_at IS NOT NULL;
--  index users
CREATE INDEX idx_users_keycloak_id ON users(keycloak_id);
CREATE INDEX idx_users_username ON users(username);

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

-- ins mock user
INSERT INTO users (keycloak_id, username, email, plan)
VALUES ('test-keycloak-id-1', 'testuser', 'test@short.uz', 'FREE');

-- Grant permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO admin;