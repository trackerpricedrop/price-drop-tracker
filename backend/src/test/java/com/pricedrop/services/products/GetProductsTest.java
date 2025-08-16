package com.pricedrop.services.products;

import com.pricedrop.services.mongo.MongoDBClient;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
class GetProductsTest {
    private MongoDBClient mongoDBClient;
    private RoutingContext context;

    @BeforeEach
    void setUp() {
        mongoDBClient = mock(MongoDBClient.class);
        context = mock(RoutingContext.class);
        when(context.get("userId")).thenReturn("uid");
    }

    @Test
    void testGetProductsConstructor(Vertx vertx, VertxTestContext testContext) {
        when(context.get("userId")).thenReturn("uid");
        when(mongoDBClient.queryRecords(any(), any())).thenReturn(io.vertx.core.Future.succeededFuture(new java.util.ArrayList<>()));
        // Mock Utility static methods to avoid NPE in Utility.buildResponse
        try (var utilMock = org.mockito.Mockito.mockStatic(com.pricedrop.Utils.Utility.class)) {
            utilMock.when(() -> com.pricedrop.Utils.Utility.buildResponse(any(), anyInt(), any())).thenAnswer(invocation -> null);
            utilMock.when(() -> com.pricedrop.Utils.Utility.createErrorResponse(anyString())).thenReturn(new JsonObject());
            utilMock.when(() -> com.pricedrop.Utils.Utility.castToClass(any(), any())).thenReturn(mock(com.pricedrop.models.Product.class));
            new GetProducts(mongoDBClient, context);
            testContext.completeNow();
        }
    }
}
