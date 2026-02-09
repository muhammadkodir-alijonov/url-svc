# 🗄️ Database Migration with Flyway

## Overview

We use **Flyway** for database schema management instead of Hibernate's auto-generation. This is production-ready and provides:

- ✅ Version-controlled schema changes
- ✅ Repeatable deployments
- ✅ Rollback capability
- ✅ Clear audit trail
- ✅ Team collaboration friendly

## Configuration

### application.properties
```properties
# Flyway Migration
quarkus.flyway.migrate-at-start=true
quarkus.flyway.baseline-on-migrate=true
quarkus.flyway.baseline-version=0
quarkus.flyway.locations=classpath:db/migration

# Hibernate (no schema generation - Flyway handles it)
quarkus.hibernate-orm.database.generation=none
```

### What This Does

- `migrate-at-start=true`: Run migrations on application startup
- `baseline-on-migrate=true`: Initialize Flyway on existing database
- `baseline-version=0`: Start version tracking from V0
- `locations`: Where to find migration SQL files
- `database.generation=none`: Disable Hibernate schema auto-generation

## Migration Files

Located in: `src/main/resources/db/migration/`

### Naming Convention

```
V{version}__{description}.sql

Examples:
V1__Initial_schema.sql
V2__Add_analytics_table.sql
V3__Add_url_tags.sql
```

**Rules:**
- Must start with `V`
- Version number (integer)
- Double underscore `__`
- Description (use underscores for spaces)
- `.sql` extension

## Current Migrations

### V1__Initial_schema.sql

Creates:
1. **users** table
   - Stores user info synced from Keycloak
   - Indexes on keycloak_id, username, email

2. **urls** table
   - Shortened URL mappings
   - Foreign key to users
   - Indexes on short_code, user_id, is_active

3. **url_clicks** table
   - Analytics and tracking
   - IP, user agent, location data
   - Indexes for analytics queries

## How It Works

### First Deployment (Fresh Database)

```
1. Application starts
2. Flyway checks database
3. Creates flyway_schema_history table
4. Runs V1__Initial_schema.sql
5. Records migration in flyway_schema_history
6. Application ready ✅
```

### Kubernetes Deployment

```yaml
# postgres-statefulset.yaml creates database
# When url-svc pod starts:
1. Connects to postgres
2. Flyway runs V1__Initial_schema.sql
3. Tables created automatically
4. Application ready
```

### Subsequent Deployments

```
1. Application starts
2. Flyway checks flyway_schema_history
3. Sees V1 already applied
4. Skips V1, runs only new migrations (V2, V3, etc.)
5. Application ready ✅
```

## Adding New Migrations

### Example: Add URL Tags Feature

**Create:** `V2__Add_url_tags.sql`

```sql
-- Add tags support to URLs
CREATE TABLE url_tags (
    id UUID PRIMARY KEY,
    url_id UUID NOT NULL,
    tag VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tags_url FOREIGN KEY (url_id) REFERENCES urls(id) ON DELETE CASCADE
);

CREATE INDEX idx_url_tags_url_id ON url_tags(url_id);
CREATE INDEX idx_url_tags_tag ON url_tags(tag);
```

**Deploy:**
```bash
# Build new image
docker build -t url-svc:v2 .

# Deploy to K8s
kubectl set image deployment/url-svc url-svc=url-svc:v2

# Flyway automatically runs V2 on startup
```

## Checking Migration Status

### Via Database

```sql
-- Check migration history
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- Example output:
installed_rank | version | description      | type | script                    | checksum    | installed_by | installed_on        | success
1              | 1       | Initial schema   | SQL  | V1__Initial_schema.sql    | 1234567890  | admin        | 2026-02-09 10:00:00 | true
2              | 2       | Add url tags     | SQL  | V2__Add_url_tags.sql      | 0987654321  | admin        | 2026-02-10 14:00:00 | true
```

### Via Logs

```
INFO  [io.quarkus.flyway.runtime.FlywayRecorder] Flyway Migrate executing ...
INFO  [org.flyway.core.internal.command.DbMigrate] Current version of schema "public": 1
INFO  [org.flyway.core.internal.command.DbMigrate] Migrating schema "public" to version "2 - Add url tags"
INFO  [org.flyway.core.internal.command.DbMigrate] Successfully applied 1 migration to schema "public"
```

## Rollback

Flyway doesn't support automatic rollback, but you can:

### Option 1: Create Undo Migration

**V3__Undo_url_tags.sql:**
```sql
DROP TABLE IF EXISTS url_tags;
```

### Option 2: Manual Rollback

```sql
-- Connect to database
psql -h localhost -p 30432 -U admin -d url_shortener

-- Manually undo changes
DROP TABLE url_tags;

-- Update Flyway history
DELETE FROM flyway_schema_history WHERE version = '2';
```

## Best Practices

### ✅ DO

- Write idempotent migrations (`CREATE TABLE IF NOT EXISTS`)
- Add indexes for performance
- Include comments for documentation
- Test migrations on dev environment first
- Keep migrations small and focused
- Version control all migration files

### ❌ DON'T

- Never modify existing migration files
- Don't delete old migration files
- Avoid data migrations in schema migrations (use separate files)
- Don't use database-specific syntax (keep portable)

## Troubleshooting

### Migration Failed

```
ERROR [org.flyway.core.internal.command.DbMigrate] Migration V2__Add_url_tags.sql failed
SQL State  : 42P01
Error Code : 0
Message    : ERROR: relation "urls" does not exist
```

**Solution:**
1. Fix SQL error
2. Increment version: `V3__Add_url_tags_fixed.sql`
3. Redeploy

### Database Out of Sync

```bash
# Reset and re-migrate (DEV ONLY!)
psql -h localhost -p 30432 -U admin -d url_shortener

DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

# Restart app - Flyway will recreate everything
```

## Development Workflow

### Local Development

```bash
# 1. Start Postgres (K8s)
kubectl port-forward -n url-shortener svc/postgres 30432:5432

# 2. Run Quarkus Dev Mode
./mvnw quarkus:dev

# Flyway runs automatically on startup
# Check logs for migration status
```

### Testing Migration

```bash
# 1. Create test database
psql -h localhost -p 30432 -U admin -c "CREATE DATABASE test_db;"

# 2. Point app to test database
%dev.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:30432/test_db

# 3. Run app - migrations apply to test_db
./mvnw quarkus:dev
```

## Production Deployment

```yaml
# url-svc-deployment.yaml
spec:
  template:
    spec:
      containers:
      - name: url-svc
        image: url-svc:v1.0
        env:
        - name: QUARKUS_FLYWAY_MIGRATE_AT_START
          value: "true"
        - name: QUARKUS_DATASOURCE_JDBC_URL
          value: "jdbc:postgresql://postgres:5432/url_shortener"
```

On deployment:
1. Pod starts
2. Flyway checks migrations
3. Applies any pending migrations
4. Application starts
5. Ready to serve traffic ✅

## Summary

| Feature | Hibernate DDL | Flyway |
|---------|---------------|--------|
| Version Control | ❌ | ✅ |
| Production Ready | ❌ | ✅ |
| Team Friendly | ❌ | ✅ |
| Rollback Support | ❌ | ✅ |
| Audit Trail | ❌ | ✅ |
| Schema Drift Detection | ❌ | ✅ |

**Flyway is the right choice for production!** ✅
