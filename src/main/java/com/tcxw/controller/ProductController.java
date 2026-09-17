package com.tcxw.controller;

import com.tcxw.annotation.RequireRole;
import com.tcxw.document.ProductDocument;
import com.tcxw.dto.PageResponse;
import com.tcxw.dto.ProductCreateRequest;
import com.tcxw.dto.ProductResponse;
import com.tcxw.dto.ProductUpdateRequest;
import com.tcxw.service.ProductIndexService;
import com.tcxw.service.ProductSearchService;
import com.tcxw.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;
    private final ProductIndexService productIndexService;

    public ProductController(ProductService productService,
                             ProductSearchService productSearchService,
                             ProductIndexService productIndexService) {
        this.productService = productService;
        this.productSearchService = productSearchService;
        this.productIndexService = productIndexService;
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return ProductResponse.from(productService.getById(id));
    }

    @GetMapping
    public PageResponse<ProductResponse> getAll(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return PageResponse.from(productService.getAll(page, size), ProductResponse::from);
    }

    @GetMapping("/search")
    public PageResponse<ProductDocument> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return productSearchService.search(keyword, category, minPrice, maxPrice, status, page, size);
    }

    @PostMapping
    public ProductResponse add(@Valid @RequestBody ProductCreateRequest body,
                               HttpServletRequest request) {
        return ProductResponse.from(productService.add(body, username(request)));
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id,
                                  @Valid @RequestBody ProductUpdateRequest body,
                                  HttpServletRequest request) {
        return ProductResponse.from(productService.update(id, body, username(request)));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, HttpServletRequest request) {
        productService.delete(id, username(request));
    }

    @RequireRole("ADMIN")
    @PostMapping("/search/rebuild")
    public Map<String, Long> rebuildIndex() {
        return Map.of("indexed", productIndexService.rebuild());
    }

    private String username(HttpServletRequest request) {
        return (String) request.getAttribute("username");
    }
}
