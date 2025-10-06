package com.emiray.goldshop.product;

import java.util.Map;

public record Product(
        String name,
        double popularityScore,   // 0..1
        double weight,            // gram
        Map<String, String> images // yellow, white, rose
) {}
