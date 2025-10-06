package com.emiray.goldshop.product;

public record ProductView(
        String name,
        double priceUsd,            // 2 ondalık
        double popularityOutOf5,    // 1 ondalık
        ProductImages images
) {
    public record ProductImages(String yellow, String white, String rose) {}
}
