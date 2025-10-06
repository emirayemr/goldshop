package com.emiray.goldshop.product;

public record ProductView(
        String name,
        double priceUsd,
        double popularityOutOf5,
        ProductImages images
) {
    public record ProductImages(String yellow, String white, String rose) {}
}
