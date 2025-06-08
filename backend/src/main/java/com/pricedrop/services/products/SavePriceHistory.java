package com.pricedrop.services.products;

import com.pricedrop.Utils.Utility;
import com.pricedrop.models.PriceHistory;
import com.pricedrop.models.Product;
import com.pricedrop.services.mongo.MongoDBClient;
import io.vertx.core.CompositeFuture;
import io.vertx.core.json.JsonObject;

import java.time.Instant;

public class SavePriceHistory {
    private final MongoDBClient mongoDBClient;
    public SavePriceHistory(MongoDBClient mongoDBClient) {
        this.mongoDBClient = mongoDBClient;
    }

    public void savePrice(JsonObject futureResult) {
        Product product = Utility.castToClass(futureResult.getJsonObject("product"), Product.class);
        JsonObject productInfo = futureResult.getJsonObject("productInfo");
        String productPrice = productInfo.getString("price");
        String productTitle = productInfo.getString("title");
        product.getUserIds().forEach(userId -> {
            PriceHistory priceHistory = new PriceHistory();
            priceHistory.setProductPrice(productPrice);
            priceHistory.setProductName(productTitle);
            priceHistory.setUserId(userId);
            priceHistory.setProductUrl(product.getProductUrl());
            priceHistory.setCaptureTime(Instant.now());
            priceHistory.setProductId(product.getProductId());
            mongoDBClient.insertRecord(JsonObject.mapFrom(priceHistory), "pricehistory");
        });
    }
}
