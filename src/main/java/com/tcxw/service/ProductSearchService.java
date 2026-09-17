package com.tcxw.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcxw.document.ProductDocument;
import com.tcxw.dto.PageResponse;
import com.tcxw.entity.Product;
import com.tcxw.exception.BusinessException;
import com.tcxw.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductSearchService {

    private static final Logger log = LoggerFactory.getLogger(ProductSearchService.class);

    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductMapper productMapper;
    private final ProductIndexService productIndexService;

    public ProductSearchService(ElasticsearchOperations elasticsearchOperations,
                                ProductMapper productMapper,
                                ProductIndexService productIndexService) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.productMapper = productMapper;
        this.productIndexService = productIndexService;
    }

    public PageResponse<ProductDocument> search(String keyword,
                                                String category,
                                                BigDecimal minPrice,
                                                BigDecimal maxPrice,
                                                Integer status,
                                                int page,
                                                int size) {
        validatePriceRange(minPrice, maxPrice);
        try {
            Criteria criteria = new Criteria();
            if (hasText(keyword)) {
                criteria = criteria.and(new Criteria("title").matches(keyword));
            }
            if (hasText(category)) {
                criteria = criteria.and(new Criteria("category").is(category));
            }
            if (minPrice != null && maxPrice != null) {
                criteria = criteria.and(new Criteria("price").between(minPrice, maxPrice));
            } else if (minPrice != null) {
                criteria = criteria.and(new Criteria("price").greaterThanEqual(minPrice));
            } else if (maxPrice != null) {
                criteria = criteria.and(new Criteria("price").lessThanEqual(maxPrice));
            }
            if (status != null) {
                criteria = criteria.and(new Criteria("status").is(status));
            }

            CriteriaQuery query = new CriteriaQuery(criteria);
            query.setPageable(PageRequest.of(page - 1, size));
            var hits = elasticsearchOperations.search(query, ProductDocument.class);
            long total = hits.getTotalHits();
            long pages = total == 0 ? 0 : (total + size - 1) / size;
            return new PageResponse<>(
                    hits.stream().map(SearchHit::getContent).toList(),
                    page, size, total, pages
            );
        } catch (RuntimeException exception) {
            log.warn("Elasticsearch search failed, falling back to MySQL", exception);
            return searchMySql(keyword, category, minPrice, maxPrice, status, page, size);
        }
    }

    private PageResponse<ProductDocument> searchMySql(String keyword,
                                                      String category,
                                                      BigDecimal minPrice,
                                                      BigDecimal maxPrice,
                                                      Integer status,
                                                      int page,
                                                      int size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(hasText(keyword), Product::getTitle, keyword)
                .eq(hasText(category), Product::getCategory, category)
                .ge(minPrice != null, Product::getPrice, minPrice)
                .le(maxPrice != null, Product::getPrice, maxPrice)
                .eq(status != null, Product::getStatus, status)
                .orderByDesc(Product::getCreateTime);
        var result = productMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResponse.from(result, productIndexService::toDocument);
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if ((minPrice != null && minPrice.signum() < 0)
                || (maxPrice != null && maxPrice.signum() < 0)) {
            throw new BusinessException("价格筛选条件不能小于0");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BusinessException("最低价格不能高于最高价格");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
