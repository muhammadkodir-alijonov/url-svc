# Swagger/OpenAPI Documentation Guide

## Overview
Your URL Shortener project now has **Swagger UI** fully integrated! 🎉

## Accessing Swagger UI

Once your application is running, you can access Swagger UI at:

```
http://localhost:8080/swagger-ui
```

Or the OpenAPI spec at:

```
http://localhost:8080/openapi
```

## What's Already Configured

### 1. **Dependencies** (pom.xml)
- ✅ `quarkus-smallrye-openapi` - Provides Swagger UI and OpenAPI spec generation

### 2. **Configuration** (application.yml)
```yaml
quarkus:
  smallrye-openapi:
    info-title: URL Shortener API
    info-version: 1.0.0
    info-description: API for URL shortening service
    
  swagger-ui:
    always-include: true
    path: /swagger-ui
    title: URL Shortener API
```

### 3. **OpenAPI Config Class** (OpenApiConfig.java)
- ✅ API metadata (title, version, description)
- ✅ Contact information
- ✅ License information
- ✅ Multiple server configurations (dev, gateway, production)
- ✅ JWT Bearer authentication scheme
- ✅ API tags for grouping endpoints

### 4. **Resource Annotations**
All your REST endpoints are already annotated with:
- `@Tag` - Groups endpoints by category
- `@Operation` - Describes each endpoint
- `@APIResponse` - Documents response codes
- `@SecurityRequirement` - Marks authenticated endpoints
- `@Parameter` - Describes path/query parameters

## API Endpoints Documented

### 🔗 URL Management (`/api/urls`)
- **POST** `/api/urls` - Shorten URL
- **GET** `/api/urls` - List my URLs (with pagination)
- **GET** `/api/urls/{shortCode}` - Get URL details
- **PUT** `/api/urls/{shortCode}` - Update URL
- **DELETE** `/api/urls/{shortCode}` - Delete URL
- **GET** `/api/urls/{shortCode}/qr` - Get QR Code

### 👤 User Management (`/api/users`)
- **POST** `/api/users/sync` - Sync user from Keycloak
- **GET** `/api/users/me` - Get current user profile

### 🔄 Redirect (`/{shortCode}`)
- **GET** `/{shortCode}` - Redirect to original URL (public)

### ❤️ Health (`/health`)
- **GET** `/health` - Health check

## Using Swagger UI with Authentication

Since your API uses JWT authentication from Keycloak:

1. **Get a JWT Token** from Keycloak:
   ```bash
   curl -X POST http://localhost:8180/realms/url-shortener/protocol/openid-connect/token \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "username=YOUR_USERNAME" \
     -d "password=YOUR_PASSWORD" \
     -d "grant_type=password" \
     -d "client_id=shortener-backend" \
     -d "client_secret=YOUR_SECRET"
   ```

2. **In Swagger UI**, click the **"Authorize"** button (🔒 icon)

3. **Enter the token** in the format:
   ```
   Bearer YOUR_JWT_TOKEN_HERE
   ```

4. **Click "Authorize"** and then "Close"

5. Now you can **test authenticated endpoints** directly from Swagger UI! 🚀

## Testing Endpoints

With Swagger UI you can:
- 📖 **Browse** all available endpoints
- 🧪 **Test** endpoints with custom parameters
- 📝 **View** request/response schemas
- 🔍 **See** example values
- 📊 **Check** response codes and error messages

## Example: Testing URL Shortening

1. Go to Swagger UI: `http://localhost:8080/swagger-ui`
2. Authorize with your JWT token
3. Find **POST** `/api/urls` under "URL Management"
4. Click **"Try it out"**
5. Enter request body:
   ```json
   {
     "originalUrl": "https://github.com",
     "customAlias": "gh",
     "title": "GitHub"
   }
   ```
6. Click **"Execute"**
7. See the response!

## Customization

You can customize Swagger UI by editing:

### application.yml
```yaml
quarkus:
  swagger-ui:
    path: /swagger-ui           # Change UI path
    title: Your API Name        # Browser tab title
    theme: feeling-blue         # UI theme (optional)
    always-include: true        # Include in production
```

### OpenApiConfig.java
- Update API metadata
- Add/modify servers
- Change contact information
- Adjust security schemes

## Production Considerations

For production, you might want to:

1. **Disable Swagger UI**:
   ```yaml
   quarkus:
     swagger-ui:
       always-include: false
   ```

2. **Keep OpenAPI spec** but disable UI:
   ```yaml
   quarkus:
     swagger-ui:
       enable: false
   ```

3. **Secure the endpoints** with authentication:
   ```yaml
   quarkus:
     http:
       auth:
         permission:
           swagger:
             paths: /swagger-ui/*,/openapi
             policy: authenticated
   ```

## Troubleshooting

### Swagger UI not loading?
- Check if the application is running
- Verify the port (default: 8080)
- Clear browser cache

### Endpoints not showing?
- Make sure `@Path` annotation is present
- Check if the resource class is in the correct package
- Verify CDI beans are discovered

### Authentication not working?
- Ensure Keycloak is running
- Verify token is not expired
- Check token format: `Bearer <token>`

## Next Steps

- ✅ Swagger/OpenAPI is fully integrated
- ✅ All endpoints are documented
- ✅ Authentication is configured
- 🎉 Start testing your API!

## Resources

- [Quarkus OpenAPI Guide](https://quarkus.io/guides/openapi-swaggerui)
- [SmallRye OpenAPI](https://github.com/smallrye/smallrye-open-api)
- [OpenAPI Specification](https://swagger.io/specification/)

---

**Happy API Testing! 🚀**
