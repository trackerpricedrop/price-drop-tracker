package com.pricedrop.services.scrape;

import com.pricedrop.models.Product;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScrapperClient {
    private final WebClient client;
    private final Logger log = LoggerFactory.getLogger(ScrapperClient.class);

    public ScrapperClient(Vertx vertx) {
        this.client = WebClient.create(vertx);
    }

    public Future<JsonObject> scrapeProductUrl(Product product) {
        JsonObject body = new JsonObject().put("urls",
                new JsonArray().add(product.getProductUrl()));
        Promise<JsonObject> promise = Promise.promise();
        String scrapperUrl = "http://scrapper:8110/scrape";
        log.info("calling scrapping api for {}", product.getProductUrl());
        try {
            client.postAbs(scrapperUrl)
                    .putHeader("Content-Type", "application/json")
                    .sendJsonObject(body).onSuccess(res -> {
                        try {
                            log.info("success in calling scrapping script {}", res.bodyAsString());
                            String resString = res.bodyAsString();
                            JsonObject productInfo = new JsonArray(resString)
                                    .getJsonObject(0)
                                    .getJsonObject("data");
                            if (productInfo.getString("price", "").isEmpty()
                                    || productInfo.getString("title", "").isEmpty()) {
                                log.error("Failed to fetch price/title for: {}", product.getProductUrl());
                                promise.fail("failed to fetch price/title");
                            } else {
                                JsonObject productObj = JsonObject.mapFrom(product);
                                promise.complete(new JsonObject()
                                        .put("productInfo", productInfo)
                                        .put("product", productObj));
                            }
                        } catch (Exception ex) {
                            log.error("exception in response processing: {}", ex.getMessage());
                            promise.fail(ex.getMessage());
                        }
                    }).onFailure(failure -> {
                        log.error("error in calling scrapping script {}", failure.getMessage());
                        promise.fail(failure.getMessage());
                    });
        } catch (Exception e) {
            log.error("error in implementing web client {}", e.getMessage());
            promise.fail(e.getMessage());
        }

        return promise.future();
    }
}
