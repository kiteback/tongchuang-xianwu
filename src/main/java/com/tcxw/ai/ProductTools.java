package com.tcxw.ai;

import com.tcxw.document.ProductDocument;
import com.tcxw.dto.PageResponse;
import com.tcxw.dto.ProductResponse;
import com.tcxw.enums.ProductStatus;
import com.tcxw.service.ProductSearchService;
import com.tcxw.service.ProductService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ProductTools {

    private final ProductSearchService productSearchService;
    private final ProductService productService;

    public ProductTools(ProductSearchService productSearchService, ProductService productService) {
        this.productSearchService = productSearchService;
        this.productService = productService;
    }

    @Tool(description = "根据用户提供的关键词搜索当前可购买的商品。返回的数据来自平台数据库，不得编造商品。")
    public PageResponse<ProductDocument> searchProducts(
            @ToolParam(description = "商品标题关键词，例如教材、自行车或耳机") String keyword) {
        return productSearchService.search(
                keyword, null, null, null,
                ProductStatus.AVAILABLE.getCode(), 1, 10
        );
    }

    @Tool(description = "根据商品ID查询商品的真实详情。仅当用户给出或搜索结果中存在商品ID时使用。")
    public ProductResponse getProduct(
            @ToolParam(description = "商品ID") Long productId) {
        return ProductResponse.from(productService.getById(productId));
    }
}
