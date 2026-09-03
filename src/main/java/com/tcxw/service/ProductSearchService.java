package com.tcxw.service;

import com.tcxw.document.ProductDocument;
import com.tcxw.repository.ProductDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductSearchService {

    private final ProductDocumentRepository productDocumentRepository;

    public ProductSearchService(ProductDocumentRepository productDocumentRepository) {
        this.productDocumentRepository = productDocumentRepository;
    }

    public Iterable<ProductDocument> findAll() {
        return productDocumentRepository.findAll();
    }

    public List<ProductDocument> search(String keyword) {
        return productDocumentRepository.searchByTitle(keyword);
    }
}