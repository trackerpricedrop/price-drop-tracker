package com.pricedrop.services.schedule;

import com.pricedrop.Utils.Utility;
import com.pricedrop.services.mongo.MongoDBClient;
import com.pricedrop.services.products.ProductChecker;
import com.pricedrop.services.scrape.ScrapperClient;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;

public class Schedule {
    public static void schedulePriceCheck(RoutingContext context,
                                          MongoDBClient mongoDBClient,
                                          Vertx vertx, WebClient client) {
        // This method will be used to schedule the price check tasks
        ScrapperClient scrapperClient = new ScrapperClient(client);
        ProductChecker productChecker = new ProductChecker(mongoDBClient, vertx, scrapperClient, client);
        productChecker.checkAllProducts();
        Utility.buildResponse(context, 200,
                Utility.createSuccessResponse("Price check scheduled successfully"));
    }
}
