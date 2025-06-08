package com.pricedrop.services.alerts;

import com.pricedrop.Utils.Utility;
import com.pricedrop.models.Product;
import com.pricedrop.models.User;
import com.pricedrop.services.batchprocessor.BatchProcessor;
import com.pricedrop.services.mongo.MongoDBClient;
import com.pricedrop.services.user.UserManagement;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertsValidator {
    private final int MARGIN = 10;
    private static final Logger log = LoggerFactory.getLogger(AlertsValidator.class);
    MongoDBClient mongoDBClient;
    Vertx vertx;

    public AlertsValidator(MongoDBClient mongoDBClient, Vertx vertx) {
        this.mongoDBClient = mongoDBClient;
        this.vertx = vertx;
    }

    public void checkForAlertsAndSend(JsonObject futureResult) {
        Product product = Utility.castToClass(futureResult.getJsonObject("product"), Product.class);
        JsonObject productInfo = futureResult.getJsonObject("productInfo");
        String productName = productInfo.getString("title");
        int productPrice = Utility.extractPrice(productInfo.getString("price"));
        int targetPrice = Utility.extractPrice(product.getTargetPrice());
        log.info("productName: {}, productPrice: {}, targetPrice: {}",
                productName,
                productPrice,
                targetPrice);
        if (productPrice <= targetPrice + ((MARGIN) * targetPrice / 100)) {
            log.info("price dropped for product: {}, user will be alerted", productName);
            UserManagement userManagement = new UserManagement(mongoDBClient);
            userManagement.fetchUsersFromUserIds(product.getUserIds()).onFailure(fail -> {
                log.error("error in fetching users");
            }).onSuccess(usersObj -> {
                usersObj.forEach(userObj -> {
                    User user = Utility.castToClass(userObj, User.class);
                    AlertClient alertClient = new AlertClient(user, productInfo, product, vertx);
                    alertClient.sendAlerts().onSuccess(res -> {
                        log.info("deleting the product: {} after alert is triggered", productName);
                        mongoDBClient.deleteRecordAsync(new JsonObject().put("productId", product.getProductId()),
                                "products");
                    });
                });
            });
        } else {
            log.info("no alerts will be triggered for: {}", productName);
        }
    }

}
