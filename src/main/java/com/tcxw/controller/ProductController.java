package com.tcxw.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tcxw.entity.Product;
import com.tcxw.service.ProductService;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.tcxw.service.ProductSearchService;
import com.tcxw.document.ProductDocument;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;

    public ProductController(ProductService productService,ProductSearchService productSearchService){
        this.productService = productService;
        this.productSearchService = productSearchService;
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id){
        return productService.getById(id);
    }

    @GetMapping
    public IPage<Product> getAll(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size ){


        return productService.getAll(page,size);
    }

    @GetMapping("/search")
    public List<ProductDocument> search(@RequestParam String keyword){
        return productSearchService.search(keyword);
    }

    @PostMapping
    public String add(@RequestBody @Valid Product product,
                      HttpServletRequest request){
        String username = (String) request.getAttribute("username");

        productService.add(product,username);

        return "商品添加成功";
    }

    @PutMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @RequestBody @Valid Product product,
            HttpServletRequest request){

        String username = (String) request.getAttribute("username");

        product.setId(id);
        productService.update(product, username);

        return "商品修改成功";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id,
                         HttpServletRequest request){
        String username = (String) request.getAttribute("username");

        productService.delete(id,username);

        return "商品删除成功";
    }



}
