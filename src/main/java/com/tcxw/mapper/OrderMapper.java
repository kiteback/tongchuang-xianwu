package com.tcxw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcxw.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}