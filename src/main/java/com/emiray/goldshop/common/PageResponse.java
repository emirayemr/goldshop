package com.emiray.goldshop.common;

import java.util.List;

public record PageResponse<T>(List<T> items, int total) {}
