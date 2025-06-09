package com.pricedrop.services.schedule;

import com.pricedrop.Utils.Utility;
import com.pricedrop.services.mongo.MongoDBClient;
import com.pricedrop.services.products.ProductChecker;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class Schedule {
    public static void schedulePriceCheck(RoutingContext context,
                                          MongoDBClient mongoDBClient,
                                          Vertx vertx) {
        // This method will be used to schedule the price check tasks
        // Implementation will depend on the scheduling library or framework used
        ProductChecker productChecker = new ProductChecker(mongoDBClient, vertx);
        productChecker.checkAllProducts();
        Utility.buildResponse(context, 200,
                Utility.createSuccessResponse("Price check scheduled successfully"));
    }
}
