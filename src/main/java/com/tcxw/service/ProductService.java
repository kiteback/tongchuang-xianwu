package com.tcxw.service;

import com.tcxw.entity.Product;
import com.baomidou.mybatisplus.core.metadata.IPage;


public interface ProductService{
    Product getById(Long id);

    IPage<Product> getAll(int page,int size);

    void add(Product product, String username);

    void update(Product product, String username);

    void delete(Long id, String username);
}

