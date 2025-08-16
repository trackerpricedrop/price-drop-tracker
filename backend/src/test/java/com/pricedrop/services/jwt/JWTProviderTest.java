package com.pricedrop.services.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JWTProviderTest {
    @Test
    void testGenerateTokenAndVerifyToken() {
        String userId = "test-user";
        String token = JWTProvider.generateToken(userId);
        assertNotNull(token);
        DecodedJWT jwt = JWTProvider.verifyToken(token);
        assertEquals(userId, jwt.getClaim("userId").asString());
        assertTrue(jwt.getExpiresAt().getTime() > System.currentTimeMillis());
    }
}

