package com.pricedrop.services.products;

import com.pricedrop.models.Product;
import com.pricedrop.models.ProductStatus;
import com.pricedrop.services.mongo.MongoDBClient;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.pricedrop.Utils.Utility.*;

public class SaveProduct {
    private final static Logger log = LoggerFactory.getLogger(SaveProduct.class);
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
        String productId = generateProductId(productUrl);
        queryForProductId(productId, userId, targetPrice).onSuccess(productStatus -> {
            if (!productStatus.isProductExists()
                    || (productStatus.isProductExists() && !productStatus.isTargetPriceExists())) {
                products.setProductId(productId);
                products.setProductUrl(productUrl);
                products.setTargetPrice(targetPrice);
                products.setUserIds(List.of(userId));
                mongoDBClient.insertRecord(JsonObject.mapFrom(products),
                        "products").onSuccess(res -> {
                    buildResponse(context, 200, createSuccessResponse("product inserted"));
                }).onFailure(fail ->
                        buildResponse(context, 500,
                                createErrorResponse("error adding product, retry!!")));
            } else {
                if (productStatus.isTargetPriceExists() && !productStatus.isUserExists()) {
                    JsonObject query = new JsonObject().put("productId", productId);
                    JsonObject updateObject = new JsonObject()
                            .put("$addToSet",
                                    new JsonObject().put("userIds", userId));
                    mongoDBClient.updateRecord(query, updateObject, "products").onSuccess(res -> {
                        buildResponse(context, 200, createSuccessResponse("product inserted"));
                    }).onFailure(fail -> buildResponse(context, 500,
                            createErrorResponse("error adding product, retry!!")));

                } else {
                    buildResponse(context, 400,
                            createErrorResponse("product with this target price already exists"));
                }
            }
        }).onFailure(fail ->
                buildResponse(context, 500,
                        createErrorResponse("error adding product, retry!!"))
        );
    }

    public Future<ProductStatus> queryForProductId(String productId, String userId, String targetPrice) {
        Promise<ProductStatus> promise = Promise.promise();
        ProductStatus productStatus = new ProductStatus();
        JsonObject query = new JsonObject().put("productId", productId);
        mongoDBClient.queryRecords(query, "products").onSuccess(res -> {
            if (!res.isEmpty()) {
                log.info("product already exists");
                productStatus.setProductExists(true);
                res.forEach(productObj -> {
                    Product product = castToClass(productObj, Product.class);
                    if (product.getTargetPrice().equals(targetPrice)) {
                        productStatus.setTargetPriceExists(true);
                        if (product.getUserIds().contains(userId)) {
                            productStatus.setUserExists(true);
                        }
                    }
                });
            }
            log.info("logging productStatus {}", JsonObject.mapFrom(productStatus));
            promise.complete(productStatus);
        }).onFailure(fail -> promise.fail(fail.getMessage()));
        return promise.future();
    }
}
