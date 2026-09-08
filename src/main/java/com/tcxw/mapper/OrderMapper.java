package com.tcxw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcxw.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Update("""
            UPDATE orders
            SET status = 4,
                update_time = NOW()
            WHERE id = #{id}
            AND status = 1
""")
    int cancelIfPending(Long id);
}