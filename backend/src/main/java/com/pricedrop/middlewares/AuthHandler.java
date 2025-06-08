package com.pricedrop.middlewares;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.pricedrop.Utils.Utility;
import com.pricedrop.services.jwt.JWTProvider;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class AuthHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext context) {
        String authHeader = context.request().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            Utility.buildResponse(context, 401, Utility.createErrorResponse("missing auth token"));
        } else {
            String token = authHeader.substring("Bearer ".length());
            try {
                DecodedJWT decodedJWT = JWTProvider.verifyToken(token);
                String userName = decodedJWT.getClaim("userName").asString();
                String userId = decodedJWT.getClaim("userId").asString();
                context.put("userName", userName);
                context.put("userId", userId);
                context.next();
            } catch (Exception e) {
                Utility.buildResponse(context, 401, Utility.createErrorResponse("invalid Token in headers"));
            }
        }

    }
}
