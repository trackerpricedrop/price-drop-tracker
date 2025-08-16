package com.pricedrop.verticles;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class PriceDropBaseVerticleTest {
    @Test
    void testVerticleDeployment(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new PriceDropBaseVerticle())
            .onComplete(ar -> {
                if (ar.succeeded()) {
                    testContext.completeNow();
                } else {
                    testContext.failNow(ar.cause());
                }
            });
    }
}
