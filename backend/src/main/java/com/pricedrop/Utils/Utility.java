package com.pricedrop.Utils;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.Locale;

import static com.pricedrop.Utils.Constants.*;

public class Utility {

    public static <T> void buildResponse(RoutingContext context, Integer statusCode, T response) {
        context.response()
                .setStatusCode(statusCode)
                .putHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE)
                .end(Json.encode(response));
    }

    public static <T> JsonObject createSuccessResponse(T message) {
        return new JsonObject().put("response", message);
    }
    public static <T> JsonObject createErrorResponse(T error) {
        return new JsonObject().put("error", error);
    }
    public static <T> T castToClass(JsonObject jsonObject, Class<T> clazz) {
        return jsonObject.mapTo(clazz);
    }
    public static String generateProductId(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(url.getBytes(StandardCharsets.UTF_8));

            // Convert byte[] to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();  // 64-character hex
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
    public static int extractPrice(String priceStr) {
        // Remove all non-digit characters except the decimal point
        String cleaned = priceStr.replaceAll("[^\\d.]", "");

        // Extract the integer part before the decimal point
        String[] parts = cleaned.split("\\.");
        return Integer.parseInt(parts[0]);
    }
    public static String formatToINR(String numberStr) {
        try {
            // Parse the input string to a number
            double number = Double.parseDouble(numberStr);

            // Create a NumberFormat instance for Indian locale
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

            // Format the number to INR currency format
            return formatter.format(number);
        } catch (NumberFormatException e) {
            // Handle invalid input
            return "Rs." + numberStr;
        }
    }
}
