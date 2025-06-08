package com.pricedrop.verticles;

import com.pricedrop.middlewares.AuthHandler;
import com.pricedrop.services.jwt.JWTProvider;
import com.pricedrop.services.products.ProductChecker;
import com.pricedrop.services.products.SaveProduct;
import com.pricedrop.services.mongo.MongoDBClient;
import com.pricedrop.services.user.UserManagement;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PriceDropBaseVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(PriceDropBaseVerticle.class);
    private MongoDBClient mongoDBClient;
    private UserManagement userManagement;
    private SaveProduct saveProduct;
    private String registerSchema;
    private JWTProvider jwtProvider;
    @Override
    public void start(Promise<Void> startFuture) throws Exception {
        try {
            JsonObject mongoConfig = loadMongoConfig();
            log.info("mongoConfig {}", mongoConfig);
            registerSchema = loadSchema("schemas/register-schema.json");
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            String mongoDbUrl = dotenv.get("DB_URL");
            mongoConfig.put("connection_string", mongoDbUrl);
            mongoDBClient = new MongoDBClient(vertx, mongoConfig);
            mongoDBClient.pingConnection().onSuccess(res -> {
                Router router = Router.router(vertx);
                router.route().handler(BodyHandler.create());
                userManagement = new UserManagement(mongoDBClient);
                router.post("/api/login").handler(context
                        -> userManagement.handleLogin(context));
                router.post("/api/register").handler(context
                        -> userManagement.handleRegister(context));
                router.route("/api/protected/*").handler(new AuthHandler());
                saveProduct = new SaveProduct(mongoDBClient);
                router.post("/api/protected/save-product").handler(context
                        -> saveProduct.saveProduct(context));
                vertx.createHttpServer()
                        .requestHandler(router)
                        .listen(8080)
                        .onSuccess(server -> {
                            log.info("Server started on port: {}", server.actualPort());
                            ProductChecker productChecker = new ProductChecker(mongoDBClient, vertx);
                            vertx.deployVerticle(new PriceCheckSchedulerVerticle(productChecker))
                                    .onSuccess(id ->
                                            log.info("price check scheduler verticle deployed with ID: {}", id))
                                    .onFailure(deployFail ->
                                    log.error("error in delploying price check verticle {}", deployFail.getMessage()));
                            startFuture.complete();
                        }).onFailure(fail -> startFuture.fail(fail.getMessage()));
            }).onFailure(fail -> startFuture.fail(fail.getMessage()));
        } catch (Exception e) {
            log.error("Exception in starting the server {}", e.getMessage());
            startFuture.fail(e.getMessage());
        }


    }

    private JsonObject loadMongoConfig() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("mongo-config.json")) {
            if (is == null) {
                throw new RuntimeException("mongo-config.json not found in resources");
            }
            // Read stream and parse JSON
            return new JsonObject(new String(is.readAllBytes()));
        }
    }

    public String loadSchema(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Schema file not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load schema: " + path, e);
        }
    }


    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        if (mongoDBClient.getMongoClient() != null) {
            mongoDBClient.getMongoClient().close();
        }
        stopPromise.complete();
    }

}
