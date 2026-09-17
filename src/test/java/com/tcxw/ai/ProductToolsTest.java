package com.tcxw.ai;

import com.tcxw.dto.PageResponse;
import com.tcxw.entity.Product;
import com.tcxw.enums.ProductStatus;
import com.tcxw.service.ProductSearchService;
import com.tcxw.service.ProductService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductToolsTest {

    @Test
    void searchToolDelegatesToAvailableProductSearch() {
        ProductSearchService searchService = mock(ProductSearchService.class);
        ProductService productService = mock(ProductService.class);
        when(searchService.search("Java教材", null, null, null,
                ProductStatus.AVAILABLE.getCode(), 1, 10))
                .thenReturn(new PageResponse<>(List.of(), 1, 10, 0, 0));

        ProductTools tools = new ProductTools(searchService, productService);
        tools.searchProducts("Java教材");

        verify(searchService).search("Java教材", null, null, null,
                ProductStatus.AVAILABLE.getCode(), 1, 10);
    }

    @Test
    void detailToolReturnsRealServiceData() {
        ProductSearchService searchService = mock(ProductSearchService.class);
        ProductService productService = mock(ProductService.class);
        Product product = new Product();
        product.setId(9L);
        product.setTitle("算法教材");
        product.setPrice(new BigDecimal("20.00"));
        product.setStatus(ProductStatus.AVAILABLE.getCode());
        when(productService.getById(9L)).thenReturn(product);

        ProductTools tools = new ProductTools(searchService, productService);
        var response = tools.getProduct(9L);

        assertEquals("算法教材", response.title());
        verify(productService).getById(9L);
    }
}
