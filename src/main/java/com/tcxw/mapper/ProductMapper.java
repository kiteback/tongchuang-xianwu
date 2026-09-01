package com.tcxw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcxw.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Update("""
            UPDATE product
            SET status = #{status},
                update_time = NOW()
            WHERE id = #{id}
              AND status = 1
            """)
    int updateStatusIfAvailable(Long id, Integer status);
}