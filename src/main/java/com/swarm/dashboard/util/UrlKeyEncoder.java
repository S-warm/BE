package com.swarm.dashboard.util;

public class UrlKeyEncoder {

    // https://automationexercise.com/products → automationexercise_com_products
    public static String encode(String url) {
        if (url == null) return null;
        return url
            .replaceFirst("^https?://", "")
            .replaceAll("[./]", "_");
    }
}
