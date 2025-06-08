package com.pricedrop.verticles;

import com.pricedrop.services.products.ProductChecker;
import io.vertx.core.AbstractVerticle;

public class PriceCheckSchedulerVerticle extends AbstractVerticle {
    ProductChecker productChecker;
    public PriceCheckSchedulerVerticle(ProductChecker productChecker) {
        this.productChecker = productChecker;
    }
    @Override
    public void start() {
        long interval = 60 * 60 *  1000; // will run every 1 hour
        vertx.setPeriodic(interval, id -> {
           productChecker.checkAllProducts();
        });

    }
}
