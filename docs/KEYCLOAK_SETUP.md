# Keycloak Realm Setup

Keycloak da `url-shortener` realm yaratish va sozlash.

## 1. Keycloak Admin Panel

http://localhost:30180

**Login:**
- Username: `admin`
- Password: `admin123`

## 2. Create Realm

1. Top-left corner: `Master` dropdown → `Create Realm`
2. **Realm name:** `url-shortener`
3. **Enabled:** Yes
4. Click `Create`

## 3. Create Client

1. Left menu: `Clients` → `Create client`
2. **Client type:** OpenID Connect
3. **Client ID:** `url-shortener-client`
4. Click `Next`

### Client Settings:
- **Client authentication:** OFF (public client)
- **Authorization:** OFF
- **Authentication flow:**
  - ✅ Standard flow
  - ✅ Direct access grants
  - ✅ Implicit flow (optional)
- Click `Next`

### Valid redirect URIs:
```
http://localhost:8080/*
http://localhost:3000/*
*
```

### Web origins:
```
http://localhost:8080
http://localhost:3000
*
```

Click `Save`

## 4. Client Roles (Optional)

1. Go to `Clients` → `url-shortener-client`
2. `Roles` tab → `Create role`
3. Create role: `user`

## 5. Realm Roles

1. Left menu: `Realm roles` → `Create role`
2. **Role name:** `user`
3. Click `Save`

## 6. Default Roles

1. Left menu: `Realm settings`
2. `User registration` tab
3. **User registration:** ON (if you want self-registration via Keycloak UI)
4. Go to `Default roles` tab
5. Click `Assign role`
6. Select `user` role
7. Click `Assign`

Now all new users will automatically get `user` role.

## 7. Token Settings (Optional)

1. Left menu: `Realm settings`
2. `Tokens` tab
3. **Access Token Lifespan:** 5 Minutes (default)
4. **Refresh Token Max Lifespan:** 30 Minutes
5. Click `Save`

## 8. Verify Setup

### Test with cURL:

```bash
# Get token
curl -X POST http://localhost:30180/realms/url-shortener/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=url-shortener-client" \
  -d "username=YOUR_USERNAME" \
  -d "password=YOUR_PASSWORD"
```

### Expected Response:
```json
{
  "access_token": "eyJhbGc...",
  "refresh_token": "eyJhbGc...",
  "token_type": "Bearer",
  "expires_in": 300
}
```

## 9. Application Configuration

Update `application-dev.properties`:

```properties
%dev.quarkus.oidc.auth-server-url=http://localhost:30180/realms/url-shortener
%dev.quarkus.oidc.client-id=url-shortener-client
%dev.quarkus.oidc.credentials.secret=  # Leave empty for public client
```

## Done!

Keycloak realm ready for use. Now you can:

1. Register users via `/api/auth/register`
2. Login via `/api/auth/login`
3. Access protected endpoints with JWT tokens

---

## Troubleshooting

### Issue: "Realm not found"
- Check Keycloak is running: `kubectl get pods -n url-shortener`
- Check realm name in URL matches configuration

### Issue: "Invalid client credentials"
- Verify client ID: `url-shortener-client`
- Ensure "Client authentication" is OFF

### Issue: "User not found"
- Create user manually in Keycloak Admin
- Or use `/api/auth/register` endpoint

### Issue: "Token expired"
- Use refresh token: `POST /api/auth/refresh`
- Check token lifespan in Realm settings
