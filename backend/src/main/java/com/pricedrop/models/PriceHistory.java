package com.pricedrop.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceHistory {
    private String productId;
    private String productName;
    private String productUrl;
    private String productPrice;
    private Instant captureTime;
    private String userId;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public Instant getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(Instant captureTime) {
        this.captureTime = captureTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
