package com.tcxw.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tcxw.dto.ProductCreateRequest;
import com.tcxw.dto.ProductUpdateRequest;
import com.tcxw.entity.Product;


public interface ProductService{
    Product getById(Long id);

    IPage<Product> getAll(int page,int size);

    Product add(ProductCreateRequest request, String username);

    Product update(Long id, ProductUpdateRequest request, String username);

    void delete(Long id, String username);
}

