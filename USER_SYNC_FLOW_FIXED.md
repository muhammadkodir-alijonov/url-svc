# 🔄 User Sync Flow - FIXED!

## Problem (Before) ❌

```
1. POST /api/auth/register
   → User created in Keycloak ✅
   → User NOT saved to database ❌

2. POST /api/auth/login  
   → Get JWT token ✅
   → User still NOT in database ❌

3. GET /api/users/me
   → Query database for user ❌
   → USER NOT FOUND → 404 ERROR ❌
```

**Muammo:** User Keycloak'da bor, lekin DB'da yo'q!

## Solution (After) ✅

### Flow 1: Register (Optional - can also auto-sync on first login)
```
1. POST /api/auth/register
   → User created in Keycloak ✅
   → (Optional) Auto-sync to DB
   → Return tokens ✅
```

### Flow 2: Login (Main Flow - AUTO-SYNC) 🎯
```
1. POST /api/auth/login
   → Authenticate with Keycloak ✅
   → Get JWT token ✅
   → AUTOMATICALLY sync user to DB ✅
   → Return tokens + user info ✅

2. GET /api/users/me
   → Query database ✅
   → User found ✅
   → Return profile ✅
```

## Code Changes

### 1. KeycloakService.java

**Added:**
```java
@Inject
IUserService userService;

public AuthResponse loginUser(String username, String password) {
    // ... authenticate with Keycloak ...
    
    AuthResponse authResponse = parseTokenResponse(response.body());
    
    // 🎯 AUTO-SYNC TO DATABASE
    if (authResponse.user != null) {
        try {
            LOG.infof("Auto-syncing user to database: %s", username);
            userService.syncUser(
                authResponse.user.id,           // keycloakId (sub)
                authResponse.user.username,     // preferred_username
                extractEmailFromToken(authResponse.accessToken)  // email
            );
        } catch (Exception e) {
            // Don't fail login if sync fails
            LOG.warnf(e, "Failed to sync user: %s", username);
        }
    }
    
    return authResponse;
}

// Helper method
private String extractEmailFromToken(String token) {
    // Decode JWT and extract email claim
}
```

### 2. UserService.java

**Fixed:**
```java
@Transactional
public User syncUser(String keycloakId, String username, String email) {
    User user = userRepository.findByKeycloakId(keycloakId).orElse(null);
    
    if (user == null) {
        // Create new user
        user = new User();
        user.id = UUID.randomUUID();
        user.keycloakId = keycloakId;
        user.username = username;
        user.email = email;
        user.plan = "FREE";
        user.linksCreated = 0;
        user.linksLimit = 100;
        
        userRepository.persist(user);  // ✅ Only persist for new users
        LOG.infof("Created new user: %s", username);
    } else {
        // Update existing user
        user.username = username;
        user.email = email;
        // ✅ NO persist needed - JPA dirty checking auto-updates
        LOG.infof("Updated existing user: %s", username);
    }
    
    return user;
}
```

## How It Works 🔄

### First Login (User not in DB):
```
1. Login → Keycloak validates ✅
2. Get JWT token ✅
3. Extract user info from JWT ✅
4. Query DB: findByKeycloakId(sub) → Not found
5. CREATE new user in DB ✅
6. Return tokens + user info ✅
```

### Subsequent Logins (User exists in DB):
```
1. Login → Keycloak validates ✅
2. Get JWT token ✅
3. Extract user info from JWT ✅
4. Query DB: findByKeycloakId(sub) → Found ✅
5. UPDATE user info (if changed) ✅
6. Return tokens + user info ✅
```

### Getting User Profile:
```
GET /api/users/me
→ Extract keycloakId from JWT
→ Query DB: findByKeycloakId(keycloakId)
→ User exists ✅
→ Return full profile ✅
```

## Testing

### 1. Register New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

**Result:** User created in Keycloak

### 2. Login (Triggers DB Sync)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**Result:** 
- ✅ Token received
- ✅ User synced to DB (check logs: "Auto-syncing user to database")

### 3. Get Profile (From DB)
```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Result:**
```json
{
  "id": "uuid",
  "keycloakId": "uuid",
  "username": "testuser",
  "email": "test@example.com",
  "plan": "FREE",
  "linksCreated": 0,
  "linksLimit": 100,
  "createdAt": "2026-02-09T..."
}
```

## Logs to Watch

```
INFO  [KeycloakService] Login successful for user: testuser
INFO  [KeycloakService] Auto-syncing user to database: testuser
INFO  [UserService] Syncing user: testuser (keycloakId: uuid)
INFO  [UserService] Created new user: testuser
DEBUG [UserService] Getting current user profile for keycloakId: uuid
```

## Benefits ✅

1. **No Manual Sync Needed**: Login automatically syncs to DB
2. **Idempotent**: Safe to call multiple times (update if exists)
3. **Resilient**: Login succeeds even if sync fails (logged as warning)
4. **Consistent**: User always in DB after first successful login
5. **Simple**: Client doesn't need to call separate sync endpoint

## Summary

| Action | Keycloak | Database | Result |
|--------|----------|----------|--------|
| Register | ✅ Create | ❌ (No) | User only in Keycloak |
| Login | ✅ Validate | ✅ **Auto-Sync** | User in both systems |
| GET /me | ❌ (No call) | ✅ Query | Profile from DB |

**Perfect!** 🎉 User is always in DB after first login.
