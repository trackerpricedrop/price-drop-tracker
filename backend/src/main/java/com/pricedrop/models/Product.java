package com.pricedrop.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    private String productId;
    private String productUrl;
    private List<String> userIds;
    private String targetPrice;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public String getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(String targetPrice) {
        this.targetPrice = targetPrice;
    }
}
