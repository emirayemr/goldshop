package com.emiray.goldshop.product;

import com.emiray.goldshop.common.PageResponse;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductsController {

    private final ProductService service;

    public ProductsController(ProductService service) {
        this.service = service;
    }

    /**
     * Returns a paginated and optionally filtered list of products.
     * Example: /api/products?minPrice=300&sortBy=price&dir=asc&page=0&size=6
     */
    @GetMapping
    public PageResponse<ProductView> list(
            @RequestParam(required = false) @Min(0) Double minPrice,
            @RequestParam(required = false) @Min(0) Double maxPrice,
            @RequestParam(required = false) @Min(0) Double minPopularity,
            @RequestParam(defaultValue = "price") SortBy sortBy,
            @RequestParam(defaultValue = "asc") Direction dir,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        return service.listAll(minPrice, maxPrice, minPopularity, sortBy, dir, page, size);
    }

    /** Sorting options for product listing */
    public enum SortBy { price, popularity, name }

    /** Sort direction (ascending or descending) */
    public enum Direction { asc, desc }
}
