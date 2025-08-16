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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
class GetPriceHistoryTest {
    private MongoDBClient mongoDBClient;
    private RoutingContext context;

    @BeforeEach
    void setUp() {
        mongoDBClient = mock(MongoDBClient.class);
        context = mock(RoutingContext.class);
    }

    @Test
    void testGetPriceHistoryConstructor(Vertx vertx, VertxTestContext testContext) {
        var requestBody = mock(io.vertx.ext.web.RequestBody.class);
        when(context.body()).thenReturn(requestBody);
        when(requestBody.asJsonObject()).thenReturn(new JsonObject().put("productId", "pid"));
        when(mongoDBClient.queryRecords(any(), any())).thenReturn(io.vertx.core.Future.succeededFuture(new java.util.ArrayList<>()));
        // Mock Utility static methods to avoid NPE in Utility.buildResponse
        try (var utilMock = org.mockito.Mockito.mockStatic(com.pricedrop.Utils.Utility.class)) {
            utilMock.when(() -> com.pricedrop.Utils.Utility.buildResponse(any(), anyInt(), any())).thenAnswer(invocation -> null);
            utilMock.when(() -> com.pricedrop.Utils.Utility.createErrorResponse(anyString())).thenReturn(new JsonObject());
            new GetPriceHistory(mongoDBClient, context);
            testContext.completeNow();
        }
    }
}
