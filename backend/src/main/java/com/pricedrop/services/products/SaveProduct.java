package com.pricedrop.services.products;

import com.pricedrop.models.Product;
import com.pricedrop.services.mongo.MongoDBClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

import static com.pricedrop.Utils.Utility.*;

public class SaveProduct {
    MongoDBClient mongoDBClient;
    public SaveProduct(MongoDBClient mongoDBClient) {
        this.mongoDBClient = mongoDBClient;
    }
    public void saveProduct(RoutingContext context) {
        JsonObject request = context.body().asJsonObject();
        String productUrl = request.getString("productUrl");
        String targetPrice = request.getString("targetPrice");
        String userId = context.get("userId");
        Product products = new Product();
        products.setProductId(generateProductId(productUrl));
        products.setProductUrl(productUrl);
        products.setTargetPrice(targetPrice);
        products.setUserIds(List.of(userId));
        mongoDBClient.insertRecord(JsonObject.mapFrom(products),
                "products").onSuccess(res -> {
            buildResponse(context, 200, createSuccessResponse("product inserted"));
        }).onFailure(fail ->
                buildResponse(context, 500,
                        createErrorResponse("error adding product, retry!!")));

    }
}
