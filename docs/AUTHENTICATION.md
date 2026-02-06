# Authentication API

## Overview

The URL Shortener service uses **Keycloak** for authentication and **JWT tokens** for API access.

## Authentication Flow

```
1. User Registration/Login → Keycloak
2. Receive JWT tokens (access + refresh)
3. Use access token in API requests
4. Sync user to local DB (automatic on first API call)
5. Refresh token when needed
```

---

## API Endpoints

### 1. Register User

**POST** `/api/auth/register`

Register a new user in Keycloak and receive JWT tokens.

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response (201 Created):**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
  "token_type": "Bearer",
  "expires_in": 300
}
```

**Errors:**
- `400` - Invalid request (missing fields, weak password)
- `409` - User already exists
- `500` - Server error

---

### 2. Login

**POST** `/api/auth/login`

Login with username/password and receive JWT tokens.

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "securePassword123"
}
```

**Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
  "token_type": "Bearer",
  "expires_in": 300
}
```

**Errors:**
- `400` - Missing credentials
- `401` - Invalid credentials
- `500` - Server error

---

### 3. Refresh Token

**POST** `/api/auth/refresh`

Refresh access token using refresh token.

**Request Body:**
```json
{
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI..."
}
```

**Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
  "token_type": "Bearer",
  "expires_in": 300
}
```

**Errors:**
- `400` - Missing refresh token
- `401` - Invalid/expired refresh token

---

### 4. Get Profile

**GET** `/api/auth/profile`

Get current authenticated user profile.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
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

**Errors:**
- `401` - Unauthorized (missing/invalid token)
- `404` - User not found (need to sync)

---

### 5. Sync User

**POST** `/api/users/sync`

Sync user from Keycloak to local database (called automatically on first API request).

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK / 201 Created):**
```json
{
  "id": "uuid",
  "keycloakId": "keycloak-user-id",
  "username": "john_doe",
  "email": "john@example.com",
  "plan": "FREE",
  "linksCreated": 0,
  "linksLimit": 100,
  "createdAt": "2026-02-06T10:00:00Z"
}
```

---

### 6. Logout

**POST** `/api/auth/logout`

Logout user (client-side token deletion).

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (204 No Content)**

---

## Using JWT Tokens

### In API Requests

Include the access token in the `Authorization` header:

```bash
curl -X POST http://localhost:8080/api/urls/shorten \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI..." \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com"}'
```

### Token Expiration

- **Access Token**: Expires in 5 minutes (300 seconds)
- **Refresh Token**: Expires in 30 minutes

When access token expires, use the refresh token to get a new one.

---

## Complete Authentication Flow Example

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

**Response:**
```json
{
  "access_token": "eyJhbGc...",
  "refresh_token": "eyJhbGc...",
  "token_type": "Bearer",
  "expires_in": 300
}
```

### 2. Sync User to Local DB

```bash
curl -X POST http://localhost:8080/api/users/sync \
  -H "Authorization: Bearer eyJhbGc..."
```

### 3. Create Short URL

```bash
curl -X POST http://localhost:8080/api/urls/shorten \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://google.com",
    "customCode": "google"
  }'
```

### 4. Get User Profile

```bash
curl -X GET http://localhost:8080/api/auth/profile \
  -H "Authorization: Bearer eyJhbGc..."
```

### 5. Refresh Token (when needed)

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refresh_token": "eyJhbGc..."
  }'
```

---

## Security Notes

1. **HTTPS Required**: Use HTTPS in production
2. **Secure Storage**: Store tokens securely (HttpOnly cookies or secure storage)
3. **Token Validation**: All protected endpoints validate JWT automatically
4. **CORS**: Configured for frontend access
5. **Password Policy**: Minimum 6 characters (configured in Keycloak)

---

## Error Responses

All errors follow this format:

```json
{
  "error": "Error message description"
}
```

Common HTTP status codes:
- `200` - Success
- `201` - Created
- `204` - No Content
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `409` - Conflict
- `500` - Server Error

---

## Frontend Integration Example

```javascript
// Register user
const register = async (username, email, password) => {
  const response = await fetch('http://localhost:8080/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, email, password })
  });
  
  const data = await response.json();
  
  // Store tokens
  localStorage.setItem('access_token', data.access_token);
  localStorage.setItem('refresh_token', data.refresh_token);
  
  // Sync user
  await syncUser(data.access_token);
  
  return data;
};

// Sync user to local DB
const syncUser = async (accessToken) => {
  await fetch('http://localhost:8080/api/users/sync', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`
    }
  });
};

// API request with token
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
    // Token expired, refresh it
    await refreshToken();
    // Retry request
    return createShortUrl(url);
  }
  
  return response.json();
};

// Refresh token
const refreshToken = async () => {
  const refresh = localStorage.getItem('refresh_token');
  
  const response = await fetch('http://localhost:8080/api/auth/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refresh_token: refresh })
  });
  
  const data = await response.json();
  
  localStorage.setItem('access_token', data.access_token);
  localStorage.setItem('refresh_token', data.refresh_token);
};
```

---

## Testing with cURL

```bash
# Set variables
export BASE_URL="http://localhost:8080"
export USERNAME="testuser"
export PASSWORD="password123"

# Register
curl -X POST $BASE_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"email\":\"test@example.com\",\"password\":\"$PASSWORD\"}" \
  | jq -r '.access_token' > token.txt

# Use token
export TOKEN=$(cat token.txt)

# Sync user
curl -X POST $BASE_URL/api/users/sync \
  -H "Authorization: Bearer $TOKEN"

# Create short URL
curl -X POST $BASE_URL/api/urls/shorten \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"url":"https://google.com"}'

# Get profile
curl -X GET $BASE_URL/api/auth/profile \
  -H "Authorization: Bearer $TOKEN"
```
