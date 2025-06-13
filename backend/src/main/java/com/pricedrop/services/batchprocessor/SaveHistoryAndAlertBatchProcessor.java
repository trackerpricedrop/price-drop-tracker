package com.pricedrop.services.batchprocessor;

import com.pricedrop.models.Product;
import com.pricedrop.services.alerts.AlertsValidator;
import com.pricedrop.services.mongo.MongoDBClient;
import com.pricedrop.services.products.SavePriceHistory;
import com.pricedrop.services.scrape.ScrapperClient;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SaveHistoryAndAlertBatchProcessor implements BatchProcessor<Product> {
    private static final Logger log = LoggerFactory.getLogger(SaveHistoryAndAlertBatchProcessor.class);
    MongoDBClient mongoDBClient;
    SavePriceHistory savePriceHistory;
    AlertsValidator alertsValidator;
    Vertx vertx;
    public SaveHistoryAndAlertBatchProcessor(MongoDBClient mongoDBClient, Vertx vertx) {
        this.mongoDBClient = mongoDBClient;
        this.alertsValidator = new AlertsValidator(mongoDBClient, vertx);
        this.savePriceHistory = new SavePriceHistory(mongoDBClient);
        this.vertx = vertx;
    }
    @Override
    public void handleBatch(int start, List<Product> products) {
       if (start >= products.size()) return;
       log.info("calling in batches with start: {}", start);
       List<Product> subListProducts = products.subList(start,
               Math.min(start + LIMIT, products.size()));
       List<Future<JsonObject>> productQueryFutures = new ArrayList<>();
       ScrapperClient scrapperClient = new ScrapperClient(vertx);
       subListProducts.forEach(product -> {
           productQueryFutures.add(scrapperClient.getScrappedProductDetails(product));
       });
       Future.join(productQueryFutures).onComplete(res -> {
           log.info("extracted all prices");
           productQueryFutures.forEach(future -> {
               if (future.succeeded()) {
                   JsonObject futureResult = future.result();
                   savePriceHistory.savePrice(futureResult);
                   alertsValidator.checkForAlertsAndSend(futureResult);
               }
           });
           handleBatch(start + LIMIT, products);
       });

    }
}
