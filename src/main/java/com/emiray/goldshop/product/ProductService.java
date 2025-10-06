package com.emiray.goldshop.product;

import com.emiray.goldshop.common.PageResponse;
import com.emiray.goldshop.price.GoldPriceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

import static com.emiray.goldshop.product.ProductsController.Direction;
import static com.emiray.goldshop.product.ProductsController.SortBy;

@Service
public class ProductService {

    private final ObjectMapper om = new ObjectMapper();
    private final GoldPriceService goldPriceService;

    public ProductService(GoldPriceService goldPriceService) {
        this.goldPriceService = goldPriceService;
    }

    public PageResponse<ProductView> listAll(Double minPrice, Double maxPrice, Double minPopularity,
                                             SortBy sortBy, Direction dir, int page, int size) {
        List<Product> products = readProducts();
        double gold = goldPriceService.getGoldPricePerGramUsd();

        List<ProductView> filtered = products.stream()
                .map(p -> toView(p, gold))
                .filter(v -> minPopularity == null || v.popularityOutOf5() >= minPopularity)
                .filter(v -> minPrice == null || v.priceUsd() >= minPrice)
                .filter(v -> maxPrice == null || v.priceUsd() <= maxPrice)
                .toList();

        // total before paging
        int total = filtered.size();

        Comparator<ProductView> cmp = switch (sortBy) {
            case popularity -> Comparator.comparing(ProductView::popularityOutOf5);
            case price -> Comparator.comparing(ProductView::priceUsd);
            case name -> Comparator.comparing(ProductView::name);
        };
        if (dir == Direction.desc) cmp = cmp.reversed();

        List<ProductView> sorted = filtered.stream().sorted(cmp).toList();

        int from = Math.min(page * size, sorted.size());
        int to = Math.min(from + size, sorted.size());
        List<ProductView> slice = sorted.subList(from, to);

        return new PageResponse<>(slice, total);
    }

    private ProductView toView(Product p, double gold) {
        double price = (p.popularityScore() + 1.0) * p.weight() * gold;
        double priceRounded = round(price, 2);
        double popularityOutOf5 = round(p.popularityScore() * 5.0, 1);

        ProductView.ProductImages imgs = new ProductView.ProductImages(
                p.images().get("yellow"),
                p.images().get("white"),
                p.images().get("rose")
        );
        return new ProductView(p.name(), priceRounded, popularityOutOf5, imgs);
    }

    private List<Product> readProducts() {
        try (InputStream is = new ClassPathResource("products.json").getInputStream()) {
            return om.readValue(is, new TypeReference<List<Product>>() {});
        } catch (Exception e) {
            throw new RuntimeException("products.json okunamadı", e);
        }
    }

    private static double round(double val, int scale) {
        return BigDecimal.valueOf(val).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }
}
