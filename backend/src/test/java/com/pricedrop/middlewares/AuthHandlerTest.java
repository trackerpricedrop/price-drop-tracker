package com.pricedrop.middlewares;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.pricedrop.Utils.Utility;
import com.pricedrop.services.jwt.JWTProvider;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class AuthHandlerTest {
    private AuthHandler authHandler;
    private RoutingContext context;

    @BeforeEach
    void setUp() {
        authHandler = new AuthHandler();
        context = mock(RoutingContext.class);
    }

    @Test
    void testHandle_MissingAuthHeader() {
        var request = mock(io.vertx.core.http.HttpServerRequest.class);
        when(context.request()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(null);
        try (MockedStatic<Utility> utilityMockedStatic = Mockito.mockStatic(Utility.class)) {
            authHandler.handle(context);
            utilityMockedStatic.verify(() -> Utility.buildResponse(eq(context), eq(401), any()));
        }
    }

    @Test
    void testHandle_InvalidToken() {
        var request = mock(io.vertx.core.http.HttpServerRequest.class);
        when(context.request()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidtoken");
        try (MockedStatic<JWTProvider> jwtProviderMockedStatic = Mockito.mockStatic(JWTProvider.class);
             MockedStatic<Utility> utilityMockedStatic = Mockito.mockStatic(Utility.class)) {
            jwtProviderMockedStatic.when(() -> JWTProvider.verifyToken("invalidtoken")).thenThrow(new RuntimeException());
            authHandler.handle(context);
            utilityMockedStatic.verify(() -> Utility.buildResponse(eq(context), eq(401), any()));
        }
    }

    @Test
    void testHandle_ValidToken() {
        var request = mock(io.vertx.core.http.HttpServerRequest.class);
        when(context.request()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken");
        var decodedJWT = mock(DecodedJWT.class);
        var claim = mock(com.auth0.jwt.interfaces.Claim.class);
        when(decodedJWT.getClaim("userId")).thenReturn(claim);
        when(claim.asString()).thenReturn("user123");
        try (MockedStatic<JWTProvider> jwtProviderMockedStatic = Mockito.mockStatic(JWTProvider.class)) {
            jwtProviderMockedStatic.when(() -> JWTProvider.verifyToken("validtoken")).thenReturn(decodedJWT);
            authHandler.handle(context);
            verify(context).put("userId", "user123");
            verify(context).next();
        }
    }
}
