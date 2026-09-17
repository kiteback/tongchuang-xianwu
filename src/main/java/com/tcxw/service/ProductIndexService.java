package com.tcxw.service;

import com.tcxw.document.ProductDocument;
import com.tcxw.entity.Product;
import com.tcxw.mapper.ProductMapper;
import com.tcxw.repository.ProductDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProductIndexService {

    private static final Logger log = LoggerFactory.getLogger(ProductIndexService.class);

    private final ProductDocumentRepository repository;
    private final ProductMapper productMapper;

    public ProductIndexService(ProductDocumentRepository repository, ProductMapper productMapper) {
        this.repository = repository;
        this.productMapper = productMapper;
    }

    public void saveBestEffort(Product product) {
        try {
            repository.save(toDocument(product));
        } catch (RuntimeException exception) {
            log.error("Failed to synchronize product {} to Elasticsearch; run index rebuild after recovery",
                    product.getId(), exception);
        }
    }

    public void deleteBestEffort(Long id) {
        try {
            repository.deleteById(id);
        } catch (RuntimeException exception) {
            log.error("Failed to delete product {} from Elasticsearch; run index rebuild after recovery",
                    id, exception);
        }
    }

    public long rebuild() {
        repository.deleteAll();
        var documents = productMapper.selectList(null).stream().map(this::toDocument).toList();
        repository.saveAll(documents);
        return documents.size();
    }

    public ProductDocument toDocument(Product product) {
        ProductDocument document = new ProductDocument();
        document.setId(product.getId());
        document.setUserId(product.getUserId());
        document.setTitle(product.getTitle());
        document.setDescription(product.getDescription());
        document.setPrice(product.getPrice());
        document.setCategory(product.getCategory());
        document.setStatus(product.getStatus());
        return document;
    }
}
