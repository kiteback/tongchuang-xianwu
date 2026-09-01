package com.tcxw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcxw.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
