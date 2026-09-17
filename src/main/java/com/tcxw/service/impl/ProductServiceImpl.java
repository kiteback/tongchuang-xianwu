package com.tcxw.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcxw.dto.ProductCreateRequest;
import com.tcxw.dto.ProductUpdateRequest;
import com.tcxw.entity.Product;
import com.tcxw.entity.User;
import com.tcxw.enums.ProductStatus;
import com.tcxw.exception.BusinessException;
import com.tcxw.exception.ForbiddenException;
import com.tcxw.exception.NotFoundException;
import com.tcxw.exception.UnauthorizedException;
import com.tcxw.mapper.ProductMapper;
import com.tcxw.mapper.UserMapper;
import com.tcxw.service.ProductIndexService;
import com.tcxw.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final ProductIndexService productIndexService;

    public ProductServiceImpl(ProductMapper productMapper,
                              UserMapper userMapper,
                              ProductIndexService productIndexService) {
        this.productMapper = productMapper;
        this.userMapper = userMapper;
        this.productIndexService = productIndexService;
    }

    @Override
    public Product getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new NotFoundException("商品不存在");
        }
        return product;
    }

    @Override
    public IPage<Product> getAll(int page, int size) {
        return productMapper.selectPage(new Page<>(page, size), null);
    }

    @Override
    @Transactional
    public Product add(ProductCreateRequest request, String username) {
        User user = requireUser(username);
        Product product = new Product();
        product.setUserId(user.getId());
        product.setTitle(request.title());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(request.category());
        product.setStatus(ProductStatus.AVAILABLE.getCode());
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(product.getCreateTime());
        productMapper.insert(product);
        productIndexService.saveBestEffort(product);
        return product;
    }

    @Override
    @Transactional
    public Product update(Long id, ProductUpdateRequest request, String username) {
        User user = requireUser(username);
        Product product = getById(id);
        assertOwnerOrAdmin(user, product);
        if (!Integer.valueOf(ProductStatus.AVAILABLE.getCode()).equals(product.getStatus())) {
            throw new BusinessException("锁定或已售商品不能修改");
        }

        if (request.title() != null) product.setTitle(request.title());
        if (request.description() != null) product.setDescription(request.description());
        if (request.price() != null) product.setPrice(request.price());
        if (request.category() != null) product.setCategory(request.category());
        productMapper.updateById(product);
        Product updated = productMapper.selectById(id);
        productIndexService.saveBestEffort(updated);
        return updated;
    }

    @Override
    @Transactional
    public void delete(Long id, String username) {
        User user = requireUser(username);
        Product product = getById(id);
        assertOwnerOrAdmin(user, product);
        if (!Integer.valueOf(ProductStatus.AVAILABLE.getCode()).equals(product.getStatus())) {
            throw new BusinessException("锁定或已售商品不能删除");
        }
        productMapper.deleteById(id);
        productIndexService.deleteBestEffort(id);
    }

    private User requireUser(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UnauthorizedException("用户不存在");
        }
        return user;
    }

    private void assertOwnerOrAdmin(User user, Product product) {
        if (!"ADMIN".equals(user.getRole()) && !product.getUserId().equals(user.getId())) {
            throw new ForbiddenException("无权操作该商品");
        }
    }
}
