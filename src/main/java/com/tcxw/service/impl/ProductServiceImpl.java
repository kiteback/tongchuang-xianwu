package com.tcxw.service.impl;

import com.tcxw.document.ProductDocument;
import com.tcxw.entity.Product;
import com.tcxw.mapper.ProductMapper;
import com.tcxw.service.ProductService;
import org.springframework.stereotype.Service;
import com.tcxw.mapper.UserMapper;
import com.tcxw.entity.User;
import com.tcxw.exception.UnauthorizedException;
import com.tcxw.exception.ForbiddenException;
import com.tcxw.exception.NotFoundException;
import com.tcxw.exception.BusinessException;
import com.tcxw.repository.ProductDocumentRepository;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final ProductDocumentRepository productDocumentRepository;

    public ProductServiceImpl(ProductMapper productMapper, UserMapper userMapper, ProductDocumentRepository productDocumentRepository) {
        this.productMapper = productMapper;
        this.userMapper = userMapper;
        this.productDocumentRepository = productDocumentRepository;
    }

    @Override
    public Product getById(Long id){

        Product product = productMapper.selectById(id);
        if(product == null){
            throw new NotFoundException("商品不存在");
        }

        return product;
    }

    @Override
    public IPage<Product> getAll(int page,int size){
        Page<Product> productPage = new Page<>(page,size);
        return productMapper.selectPage(productPage,null);
    }

    @Override
    public void add(Product product,String username){

        User user = userMapper.findByUsername(username);

        if(user == null){
            throw new UnauthorizedException("用户不存在");

        }

        product.setUserId(user.getId());
        product.setStatus(1);
        productMapper.insert(product);

        ProductDocument document = new ProductDocument();
        document.setId(product.getId());
        document.setUserId(product.getUserId());
        document.setTitle(product.getTitle());
        document.setDescription(product.getDescription());
        document.setPrice(product.getPrice());
        document.setCategory(product.getCategory());
        document.setStatus(product.getStatus());

        productDocumentRepository.save(document);
    }

    @Override
    public void update(Product product, String username){
        User user = userMapper.findByUsername(username);

        if(user == null){
            throw new UnauthorizedException("用户不存在");

        }

        Product oldProduct = productMapper.selectById(product.getId());

        if(oldProduct == null){
            throw new NotFoundException("商品不存在");
        }

        if(!"ADMIN".equals(user.getRole()) && !oldProduct.getUserId().equals(user.getId())){
            throw new ForbiddenException("无权修改该商品");
        }

        product.setUserId(oldProduct.getUserId());

        if(!"ADMIN".equals(user.getRole())
                  && product.getStatus() != null
                  && product.getStatus() == 2){
            throw new BusinessException("普通用户不能直接将商品设置为已售出");

        }
        productMapper.updateById(product);

        Product updatedProduct = productMapper.selectById(product.getId());

        ProductDocument document = new ProductDocument();
        document.setId(updatedProduct.getId());
        document.setUserId(updatedProduct.getUserId());
        document.setTitle(updatedProduct.getTitle());
        document.setDescription(updatedProduct.getDescription());
        document.setPrice(updatedProduct.getPrice());
        document.setCategory(updatedProduct.getCategory());
        document.setStatus(updatedProduct.getStatus());

        productDocumentRepository.save(document);

    }

    @Override
    public void delete(Long id, String username){

        User user = userMapper.findByUsername(username);

        if(user == null){
            throw new UnauthorizedException("用户不存在");
        }

        Product oldProduct = productMapper.selectById(id);

        if(oldProduct == null){
            throw new NotFoundException("商品不存在");
        }

        if(!"ADMIN".equals(user.getRole())
                 && !oldProduct.getUserId().equals(user.getId())){
            throw new ForbiddenException("无权删除该商品");
        }
        productMapper.deleteById(id);

        productDocumentRepository.deleteById(id);
    }

}
