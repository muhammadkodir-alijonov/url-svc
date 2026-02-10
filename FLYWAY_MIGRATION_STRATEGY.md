# 🔄 Flyway Migration Strategy

## Qanday Ishlaydi?

### Dev Mode (localhost)
```properties
%dev.quarkus.flyway.clean-at-start=true
```
**Har restart'da:**
1. ✅ DROP all tables
2. ✅ Run V1, V2, V3... (barchasini qayta)
3. ✅ V1'ni o'zgartira olasiz

### Production Mode (K8s)
```properties
quarkus.flyway.clean-at-start=false
```
**Deploy'da:**
1. ✅ Faqat yangi migration'larni run qiladi
2. ❌ V1'ni qayta run qilmaydi
3. ❌ V1'ni o'zgartirsangiz - ERROR!

---

## Migration History Table

```sql
SELECT * FROM flyway_schema_history;
```

| installed_rank | version | description          | script                      | checksum    | success |
|----------------|---------|----------------------|-----------------------------|-------------|---------|
| 1              | 1       | Initial schema       | V1__Initial_schema.sql      | 1143269200  | true    |
| 2              | 2       | Add user names       | V2__Add_user_names.sql      | 987654321   | true    |

Flyway bu table'ga qarab:
- ✅ Qaysi version'lar run bo'lganini biladi
- ✅ Checksum'ni tekshiradi
- ✅ Faqat yangilarini run qiladi

---

## Scenarios

### Scenario 1: Dev'da Schema O'zgartirish ✅

**Hozirgingiz:**
```
src/main/resources/db/migration/
  └── V1__Initial_schema.sql
```

**O'zgartirish:**
1. V1 file'ni o'zgartirasiz (yangi column qo'shasiz)
2. Restart: `s` bosing
3. Dev mode clean + re-migrate qiladi ✅

**Logs:**
```
INFO [Flyway] Cleaning schema "public"
INFO [Flyway] Migrating to version "1 - Initial schema"
INFO [Flyway] Successfully applied 1 migration
```

---

### Scenario 2: Production Deploy (Yangi Column) ✅

**Hozirgingiz (prod'da V1 allaqachon applied):**
```
flyway_schema_history:
  V1 - applied ✅
```

**Yangi column kerak:**
1. ❌ V1'ga TEGMANG!
2. ✅ Yangi file yarating: `V2__Add_full_name.sql`
3. Deploy qiling

**V2__Add_full_name.sql:**
```sql
ALTER TABLE users 
    ADD COLUMN IF NOT EXISTS full_name VARCHAR(200);
```

**Logs:**
```
INFO [Flyway] Current version: 1
INFO [Flyway] Migrating to version "2 - Add full name"
INFO [Flyway] Successfully applied 1 migration
```

**Natija:**
```
flyway_schema_history:
  V1 - applied ✅
  V2 - applied ✅ (new!)
```

---

### Scenario 3: V1'ni O'zgartirsangiz (Production) ❌

**Noto'g'ri:**
```sql
-- V1__Initial_schema.sql (modified)
CREATE TABLE users (
    -- added new column here
    full_name VARCHAR(200)  -- ❌ Don't do this!
);
```

**Deploy qilsangiz:**
```
ERROR: Migration checksum mismatch
V1 checksum: 1143269200 (old)
V1 checksum: 9876543210 (new - DIFFERENT!)
```

**Yechim:**
```bash
# Option 1: Revert V1 changes
git checkout V1__Initial_schema.sql

# Option 2: Create V2
# (recommended!)
```

---

## Best Practices

### ✅ DO:

1. **Dev'da V1'ni o'zgartiring** - clean-at-start=true ishlaydi
2. **Prod'da yangi V2, V3... yarating** - versioned approach
3. **Idempotent SQL yozing:**
   ```sql
   CREATE TABLE IF NOT EXISTS ...
   ALTER TABLE ... ADD COLUMN IF NOT EXISTS ...
   ```
4. **Test qiling** - dev'da test, keyin prod'ga deploy
5. **Rollback plan** - har migration uchun undo script yozing

### ❌ DON'T:

1. **Applied migration'ni o'zgartirmang** (prod'da)
2. **Version raqamlarini skip qilmang** (V1, V3 ❌ → V1, V2, V3 ✅)
3. **Manual SQL run qilmang** - faqat Flyway orqali
4. **Destructive operation qilmang warning'siz:**
   ```sql
   DROP TABLE users;  -- ❌ Dangerous!
   ```

---

## Your Current Setup

### Dev Mode (localhost)
```properties
# application-dev.properties
%dev.quarkus.flyway.clean-at-start=true      # ✅ Clean on restart
%dev.quarkus.flyway.repair-at-start=true     # ✅ Auto-repair checksum
```

**Natija:** Har restart'da V1'dan boshlab qayta run bo'ladi

### Production Mode (K8s)
```properties
# application.properties
quarkus.flyway.clean-at-start=false           # ✅ Never clean
quarkus.flyway.repair-at-start=true           # ✅ Repair if needed
```

**Natija:** Faqat yangi migration'larni run qiladi

---

## Migration File Naming

```
V{VERSION}__{DESCRIPTION}.sql

Examples:
✅ V1__Initial_schema.sql
✅ V2__Add_user_names.sql
✅ V3__Add_url_clicks_index.sql
✅ V4__Remove_deprecated_columns.sql
✅ V10__Major_refactoring.sql

❌ v1_initial.sql                    (lowercase v)
❌ migration_1.sql                   (no V prefix)
❌ V1_Initial_schema.sql             (single underscore)
❌ V1.1__Hotfix.sql                  (decimal version)
```

---

## Commands

### Check Migration Status
```sql
-- Connect to DB
psql -h localhost -p 30432 -U admin -d url_shortener

-- View history
SELECT installed_rank, version, description, success 
FROM flyway_schema_history 
ORDER BY installed_rank;
```

### Force Clean (DEV ONLY!)
```sql
-- Drop everything
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

-- Restart app - Flyway will recreate
```

### Repair Checksum (if mismatch)
```properties
# Temporarily enable in application.properties
quarkus.flyway.repair-at-start=true
```

---

## Summary

| Environment | V1 Modified | Behavior |
|-------------|-------------|----------|
| **Dev** | ✅ Allowed | Clean + re-migrate all |
| **Prod** | ❌ Error | Create V2 instead |

| Action | Dev | Prod |
|--------|-----|------|
| First run | V1 | V1 |
| Second run (no changes) | V1 (clean+rerun) | Nothing (V1 already applied) |
| Second run (V2 added) | V1, V2 (clean+rerun all) | V2 only (V1 already applied) |
| Modify V1 | OK (clean+rerun) | ERROR (checksum mismatch) |

---

## Conclusion

**Sizning savolingizga javob:**

**Dev'da:** Har safar **barchasini** qayta run qiladi (clean-at-start=true) ✅

**Prod'da:** Faqat **yangilarini** qo'shadi, eskisini skip qiladi ✅

**To'g'ri yondashuv:**
- Dev: V1'ni erkin o'zgartiring
- Prod: Yangi V2, V3... file'lar yarating

Perfect! 🎯
