package com.pricedrop.services.user;

import com.pricedrop.Utils.Utility;
import com.pricedrop.models.AuthProvider;
import com.pricedrop.models.User;
import com.pricedrop.services.jwt.JWTProvider;
import com.pricedrop.services.mongo.MongoDBClient;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.json.schema.*;
import io.vertx.json.schema.impl.SchemaValidatorInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.pricedrop.Utils.Constants.*;

public class UserManagement {
    private static final Logger log = LoggerFactory.getLogger(UserManagement.class);
    private final MongoDBClient mongoClient;

    public UserManagement(MongoDBClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public Future<List<JsonObject>> fetchUsersFromUserIds(List<String> userIds) {
        JsonObject query = new JsonObject().put("userId",
                new JsonObject().put("$in", new JsonArray(userIds)));
        Promise<List<JsonObject>> promise = Promise.promise();
        mongoClient.queryRecords(query, "users").onSuccess(res -> {
            if (!res.isEmpty()) {
                log.info("logging users {}", res);
                promise.complete(res);
            } else promise.fail("no user found");
        }).onFailure(fail -> promise.fail(fail.getMessage()));
        return promise.future();
    }

    public void handleLogin(RoutingContext context) {
        JsonObject requestBody = context.body().asJsonObject();
        String userName = requestBody.getString(USER_NAME, "");
        String password = requestBody.getString(PASSWORD, "");
        if (userName.isEmpty() || password.isEmpty()) {
            Utility.buildResponse(context, 400, Utility.createSuccessResponse("userName/password is empty"));
        } else {
            JsonObject query = new JsonObject().put("providerUserId", userName).put("provider", "local");
            mongoClient.queryRecords(query, "authprovider").onSuccess(authRes -> {
               if (authRes.isEmpty()) {
                   Utility.buildResponse(context, 401, "user doesnt exist");
               } else {
                   JsonObject authProvider = authRes.get(0);
                   String userId = authProvider.getString("userId");
                   String hashedPassword = authProvider.getString("hashedPassword");
                   mongoClient.queryRecords(new JsonObject().put("userId", userId), "users").onSuccess(res -> {
                       if (res.isEmpty()) {
                           Utility.buildResponse(context, 401, "user doesnt exist");
                       } else {
                           JsonObject user = res.get(0);
                           if (PasswordUtil.checkPassword(password, hashedPassword)) {
                               String jwtToken = JWTProvider.generateToken(userName, userId);
                               JsonObject response = new JsonObject().put("user", user).put("token", jwtToken);
                               Utility.buildResponse(context, 200, response);
                               Instant now = Instant.now();
                               JsonObject update = new JsonObject().put("updatedAt", now);
                               JsonObject findUpdateQueryObj = new JsonObject().put("userId", userId);
                               mongoClient.updateRecordAsync(findUpdateQueryObj, update, "users");
                               mongoClient.updateRecordAsync(findUpdateQueryObj, update, "authprovider");
                           } else Utility.buildResponse(context, 401, Utility.createErrorResponse("invalid user/password combination"));
                       }
                   }).onFailure(userTableFailure -> {
                       Utility.buildResponse(context, 500, "login failure, retry");
                   });
               }
            }).onFailure(fail -> {
                Utility.buildResponse(context, 500, "login failure, retry");
            });
        }
    }

    public void handleRegister(RoutingContext context) {
        try {
            JsonObject requestBody = context.body().asJsonObject();
//            JsonSchema registerSchema = JsonSchema.of(new JsonObject(registerSchemaString));
//            Validator validator = Validator.create(registerSchema, new JsonSchemaOptions());
//            OutputUnit outputUnit = validator.validate(requestBody);
//            if (outputUnit.getValid()) {
                log.info("register request validated");
                User user = Utility.castToClass(requestBody, User.class);
                String userId = UUID.randomUUID().toString();
                user.setUserId(userId);
                Instant now = Instant.now();
                user.setCreatedAt(now);
                user.setUpdatedAt(now);
                String hashedPassword = PasswordUtil.hashPassword(requestBody.getString("password"));
                AuthProvider authProvider = new AuthProvider(userId,
                        "local", user.getUserName(), user.getEmail(), now, now, hashedPassword);
                mongoClient.queryRecords(new JsonObject().put("userName", user.getUserName()), "users").onComplete(handler -> {
                    if (handler.succeeded()) {
                        if (!handler.result().isEmpty()) {
                            Utility.buildResponse(context, 400, Utility.createErrorResponse("user already exists, please login"));
                        } else {
                            mongoClient.insertRecord(JsonObject.mapFrom(user), "users").onSuccess(res -> {
                                mongoClient.insertRecord(JsonObject.mapFrom(authProvider), "authprovider").onSuccess(authRes -> {
                                    Utility.buildResponse(context, 200, Utility.createSuccessResponse("user is registered"));
                                }).onFailure(failure -> {
                                    Utility.buildResponse(context, 500, "failure in registering user, please retry");
                                    mongoClient.deleteRecordAsync(new JsonObject().put("userId", userId), "users");
                                });
                            }).onFailure(fail -> {
                                Utility.buildResponse(context, 500, "failure in registering user, please retry");
                            });
                        }
                    } else {
                        Utility.buildResponse(context, 500, "failure in registering user, please retry");
                    }
                });
//            } else {
//                log.info("error in validating request");
//                Utility.buildResponse(context, 400, Utility.createErrorResponse(outputUnit.getError()));
//            }
        } catch (Exception e) {
            log.info("error {}", e.toString());
            Utility.buildResponse(context, 500, Utility.createErrorResponse(e.toString()));
        }
    }
}