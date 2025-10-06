package com.emiray.goldshop.product;

import java.util.Map;

public record Product(
        String name,
        double popularityScore,
        double weight,
        Map<String, String> images
) {}
