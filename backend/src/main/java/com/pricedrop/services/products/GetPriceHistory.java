package com.pricedrop.services.products;

import com.pricedrop.Utils.Utility;
import com.pricedrop.services.mongo.MongoDBClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class GetPriceHistory {
    MongoDBClient mongoDBClient;
    public GetPriceHistory(MongoDBClient mongoDBClient, RoutingContext context) {
        this.mongoDBClient = mongoDBClient;
        this.fetchPriceHistory(context);
    }
    private void fetchPriceHistory(RoutingContext context) {
        String productId = context.body().asJsonObject().getString("productId");
        JsonObject query = new JsonObject().put("productId", productId);
        mongoDBClient.queryRecords(query, "pricehistory").onSuccess(response -> {
            Utility.buildResponse(context, 200, response);
        }).onFailure(fail -> Utility.buildResponse(context, 500,
                Utility.createErrorResponse(fail.getMessage())));
    }

}
