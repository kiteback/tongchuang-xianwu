package com.tcxw.repository;

import com.tcxw.document.ProductDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, Long> {

    @Query("""
        {
          "match": {
            "title": "?0"
          }
        }
        """)
    List<ProductDocument> searchByTitle(String keyword);
}