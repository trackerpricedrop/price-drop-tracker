package com.pricedrop.services.alerts;

import com.pricedrop.models.Product;
import com.pricedrop.models.User;
import com.pricedrop.models.UserTargetPrices;
import com.pricedrop.services.mongo.MongoDBClient;
import com.pricedrop.services.user.UserManagement;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.mockito.Mockito.*;

class AlertsValidatorTest {
    private MongoDBClient mongoDBClient;
    private Vertx vertx;
    private WebClient client;
    private AlertsValidator alertsValidator;
    private UserManagement userManagement;

    @BeforeEach
    void setUp() {
        mongoDBClient = mock(MongoDBClient.class);
        vertx = mock(Vertx.class);
        client = mock(WebClient.class);
        alertsValidator = new AlertsValidator(mongoDBClient, vertx, client);
        userManagement = mock(UserManagement.class);
    }

    @Test
    void testCheckForAlertsAndSend_NoUsersToAlert() {
        Product product = mock(Product.class);
        when(product.getUserTargetPrices()).thenReturn(List.of());
        JsonObject productInfo = new JsonObject().put("price", "1000");
        JsonObject futureResult = new JsonObject()
                .put("product", new JsonObject())
                .put("productInfo", productInfo);
        try (MockedStatic<com.pricedrop.Utils.Utility> utilMock = mockStatic(com.pricedrop.Utils.Utility.class)) {
            utilMock.when(() -> com.pricedrop.Utils.Utility.castToClass(any(), eq(Product.class))).thenReturn(product);
            utilMock.when(() -> com.pricedrop.Utils.Utility.extractPrice(anyString())).thenReturn(1000);
            alertsValidator.checkForAlertsAndSend(futureResult);
        }
    }
}

