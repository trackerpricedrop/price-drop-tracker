package com.pricedrop.services.alerts;

import com.pricedrop.Utils.Utility;
import com.pricedrop.models.Product;
import com.pricedrop.models.User;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import io.vertx.core.Future;
import io.vertx.core.Promise;

class AlertClientTest {
    private User user;
    private JsonObject productInfo;
    private Product product;
    private Vertx vertx;
    private WebClient client;
    private AlertClient alertClient;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        productInfo = new JsonObject().put("price", "1000").put("title", "Test Product");
        product = mock(Product.class);
        vertx = mock(Vertx.class);
        client = mock(WebClient.class);
        alertClient = new AlertClient(user, productInfo, product, vertx, client);
    }

    @Test
    void testCreateBody() {
        when(product.getProductUrl()).thenReturn("http://example.com");
        try (MockedStatic<Utility> utilMock = mockStatic(Utility.class)) {
            utilMock.when(() -> Utility.formatToINR("1000")).thenReturn("₹1,000");
            String body = alertClient.createBody();
            assertTrue(body.contains("Test Product"));
            assertTrue(body.contains("₹1,000"));
            assertTrue(body.contains("http://example.com"));
        }
    }

    @Test
    void testSendAlerts() {
        when(user.getEmail()).thenReturn("test@example.com");
        when(product.getProductUrl()).thenReturn("http://example.com");
        try (MockedStatic<Utility> utilMock = mockStatic(Utility.class)) {
            utilMock.when(() -> Utility.formatToINR(anyString())).thenReturn("₹1,000");
            // Mock MailService and its sendEmail method
            EmailAlertService emailAlertService = mock(EmailAlertService.class);
            when(emailAlertService.sendEmail(anyString(), anyString(), anyString())).thenReturn(Future.succeededFuture());
            // Patch AlertClient to use the mock MailService
            AlertClient alertClientSpy = spy(new AlertClient(user, productInfo, product, vertx, client));
            doReturn(emailAlertService).when(alertClientSpy).createMailService(any());
            // Actually call sendAlerts
            alertClientSpy.sendAlerts();
        }
    }

    // Add this to AlertClientTest to allow patching MailService
    // (You may need to add a protected method in AlertClient)
}
