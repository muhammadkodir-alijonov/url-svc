# 🔐 Authentication & User Registration - Complete Guide

## ✅ Authentication System Implemented!

Sizning URL Shortener projectingizga **to'liq authentication system** qo'shildi:

### Qo'shilgan Features:

1. **User Registration** - Keycloak orqali yangi user yaratish
2. **User Login** - Username/password bilan kirish
3. **JWT Tokens** - Access & Refresh tokens
4. **User Profile** - User ma'lumotlarini olish
5. **Token Refresh** - Token yangilash
6. **User Sync** - Keycloak dan local DB ga sync qilish

---

## 📋 API Endpoints

### 1. **Register** - Yangi user yaratish
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securepass123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response:**
```json
{
  "access_token": "eyJhbGc...",
  "refresh_token": "eyJhbGc...",
  "token_type": "Bearer",
  "expires_in": 300
}
```

---

### 2. **Login** - Mavjud user kirish
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "securepass123"
}
```

**Response:** (Register bilan bir xil)

---

### 3. **Get Profile** - User ma'lumotlari
```http
GET /api/auth/profile
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "id": "uuid",
  "username": "john_doe",
  "email": "john@example.com",
  "plan": "FREE",
  "links_created": 5,
  "links_limit": 100,
  "created_at": "2026-02-06T10:00:00Z"
}
```

---

### 4. **Refresh Token** - Token yangilash
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refresh_token": "eyJhbGc..."
}
```

---

### 5. **Sync User** - Keycloak → Local DB
```http
POST /api/users/sync
Authorization: Bearer <access_token>
```

Bu endpoint **avtomatik** chaqiriladi birinchi API request da.

---

## 🚀 Test Qilish

### 1. Keycloak Tayyor Bo'lishini Tekshiring

```bash
curl http://localhost:30180/health/ready
```

### 2. User Register Qilish

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

**Javob:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 300
}
```

### 3. Token ni Saqlash

```bash
export TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 4. User Sync (avtomatik yoki manual)

```bash
curl -X POST http://localhost:8080/api/users/sync \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Short URL Yaratish (authenticated)

```bash
curl -X POST http://localhost:8080/api/urls/shorten \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"url": "https://google.com", "customCode": "google"}'
```

### 6. User Profile Olish

```bash
curl http://localhost:8080/api/auth/profile \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🔧 Configuration

### application-dev.properties

```properties
# Keycloak
%dev.quarkus.oidc.auth-server-url=http://localhost:30180/realms/url-shortener
%dev.quarkus.oidc.client-id=url-shortener-client

# Public endpoints (no auth required)
%dev.quarkus.http.auth.permission.public.paths=/api/auth/register,/api/auth/login,/api/auth/refresh,/q/*,/{shortCode}
%dev.quarkus.http.auth.permission.public.policy=permit

# Authenticated endpoints
%dev.quarkus.http.auth.permission.authenticated.paths=/api/*
%dev.quarkus.http.auth.permission.authenticated.policy=authenticated
```

---

## 📁 Yaratilgan Fayllar

### DTO lar:
- `AuthRequest.java` - Register/Login request
- `AuthResponse.java` - JWT tokens response
- `UserProfileResponse.java` - User profile

### Services:
- `KeycloakService.java` - Keycloak bilan ishlash (register, login, refresh)

### Resources:
- `AuthResource.java` - Authentication endpoints
- `UserResource.java` - User sync va profile (already existed)

### Documentation:
- `docs/AUTHENTICATION.md` - To'liq API documentation

---

## 🔒 Security

### Public Endpoints (Auth kerak emas):
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /{shortCode}` - Redirect
- `/q/*` - Swagger UI, Health checks

### Protected Endpoints (Token kerak):
- `POST /api/urls/shorten`
- `GET /api/urls`
- `POST /api/users/sync`
- `GET /api/auth/profile`
- va boshqalar...

---

## 💡 Authentication Flow

```
1. Frontend → POST /api/auth/register
   ↓
2. Backend → Keycloak (create user)
   ↓
3. Backend → Keycloak (get tokens)
   ↓
4. Backend → Frontend (return tokens)
   ↓
5. Frontend → POST /api/users/sync (with token)
   ↓
6. Backend → Create user in local DB
   ↓
7. Frontend → Use token for API calls
   ↓
8. Token expires → POST /api/auth/refresh
   ↓
9. Get new tokens → Continue API calls
```

---

## 🧪 Frontend Integration Example

```javascript
// Register
const register = async (userData) => {
  const response = await fetch('http://localhost:8080/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(userData)
  });
  
  const tokens = await response.json();
  
  // Save tokens
  localStorage.setItem('access_token', tokens.access_token);
  localStorage.setItem('refresh_token', tokens.refresh_token);
  
  // Sync user
  await syncUser(tokens.access_token);
  
  return tokens;
};

// Sync user
const syncUser = async (token) => {
  await fetch('http://localhost:8080/api/users/sync', {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` }
  });
};

// Make authenticated request
const createShortUrl = async (url) => {
  const token = localStorage.getItem('access_token');
  
  const response = await fetch('http://localhost:8080/api/urls/shorten', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ url })
  });
  
  if (response.status === 401) {
    // Token expired, refresh
    await refreshAccessToken();
    return createShortUrl(url); // Retry
  }
  
  return response.json();
};

// Refresh token
const refreshAccessToken = async () => {
  const refreshToken = localStorage.getItem('refresh_token');
  
  const response = await fetch('http://localhost:8080/api/auth/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refresh_token: refreshToken })
  });
  
  const tokens = await response.json();
  
  localStorage.setItem('access_token', tokens.access_token);
  localStorage.setItem('refresh_token', tokens.refresh_token);
};
```

---

## 📚 Qo'shimcha Ma'lumot

**To'liq API documentation:** `docs/AUTHENTICATION.md`

**Token expiration:**
- Access Token: 5 minutes
- Refresh Token: 30 minutes

**Password requirements:**
- Minimum 6 characters
- (Keycloak da qo'shimcha qoidalar qo'shish mumkin)

**User roles:**
- `user` - Standard user role (default)

---

## ✅ Tayyor!

Endi sizning URL Shortener projectingiz:

✅ User registration  
✅ User login  
✅ JWT authentication  
✅ Protected API endpoints  
✅ Token refresh  
✅ User profiles  

**Keyingi qadam:** Applicationni run qilib test qiling!

```bash
./mvnw quarkus:dev
```

So'ngra:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"test123"}'
```

🎉 **Tabriklayman! Authentication system tayyor!**
