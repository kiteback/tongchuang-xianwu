package com.tcxw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tcxw.dto.OrderCreateRequest;
import com.tcxw.entity.Order;
import com.tcxw.producer.OrderMessageProducer;
import com.tcxw.message.OrderMessage;
import com.tcxw.entity.Product;
import com.tcxw.entity.User;
import com.tcxw.exception.BusinessException;
import com.tcxw.exception.ForbiddenException;
import com.tcxw.exception.NotFoundException;
import com.tcxw.exception.UnauthorizedException;
import com.tcxw.mapper.OrderMapper;
import com.tcxw.mapper.ProductMapper;
import com.tcxw.mapper.UserMapper;
import com.tcxw.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final OrderMessageProducer orderMessageProducer;

    public OrderServiceImpl(OrderMapper orderMapper,
                            ProductMapper productMapper,
                            UserMapper userMapper,
                            OrderMessageProducer orderMessageProducer) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
        this.userMapper = userMapper;
        this.orderMessageProducer = orderMessageProducer;
    }

    @Override
    @Transactional
    public Order create(OrderCreateRequest request, String username) {

        User buyer = userMapper.findByUsername(username);

        if (buyer == null) {
            throw new UnauthorizedException("用户不存在");
        }

        Product product = productMapper.selectById(request.getProductId());

        if (product == null) {
            throw new NotFoundException("商品不存在");
        }

        if (!Integer.valueOf(1).equals(product.getStatus())) {
            throw new BusinessException("商品当前不可购买");
        }

        int productUpdated = productMapper.updateStatusIfAvailable(
                product.getId(),2
        );
        if(productUpdated == 0){
            throw new BusinessException("商品已被其他用户锁定");
        }

        if (product.getUserId().equals(buyer.getId())) {
            throw new BusinessException("不能购买自己的商品");
        }

        Order order = new Order();

        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setBuyerId(buyer.getId());
        order.setSellerId(product.getUserId());
        order.setProductId(product.getId());
        order.setPrice(product.getPrice());
        order.setStatus(1);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        orderMapper.insert(order);

        OrderMessage message = new OrderMessage(
                order.getId(),
                buyer.getId(),
                "ORDER_CREATED"
        );

        orderMessageProducer.sendOrderCreatedMessage(message);
        orderMessageProducer.sendOrderTimeoutMessage(message);


        return order;
    }

    @Override
    public Order getById(Long id, String username) {

        User user = userMapper.findByUsername(username);

        if (user == null) {
            throw new UnauthorizedException("用户不存在");
        }

        Order order = orderMapper.selectById(id);

        if (order == null) {
            throw new NotFoundException("订单不存在");
        }

        if (!"ADMIN".equals(user.getRole())
                && !order.getBuyerId().equals(user.getId())
                && !order.getSellerId().equals(user.getId())) {
            throw new ForbiddenException("无权查看该订单");
        }

        return order;
    }

    @Override
    public List<Order> getMyOrders(String username) {

        User user = userMapper.findByUsername(username);

        if (user == null) {
            throw new UnauthorizedException("用户不存在");
        }

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Order::getBuyerId, user.getId())
                .or()
                .eq(Order::getSellerId, user.getId())
                .orderByDesc(Order::getCreateTime);

        return orderMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void pay(Long id, String username) {

        User user = userMapper.findByUsername(username);

        if (user == null) {
            throw new UnauthorizedException("用户不存在");
        }

        Order order = orderMapper.selectById(id);

        if (order == null) {
            throw new NotFoundException("订单不存在");
        }

        if (!order.getBuyerId().equals(user.getId())) {
            throw new ForbiddenException("只有买家可以支付订单");
        }

        if (!Integer.valueOf(1).equals(order.getStatus())) {
            throw new BusinessException("当前订单不能支付");
        }

        Product product = productMapper.selectById(order.getProductId());

        if (product == null) {
            throw new NotFoundException("商品不存在");
        }

        if(!Integer.valueOf(2).equals(product.getStatus())){
            throw new BusinessException("商品当前不是待支付状态");
        }

        int updated = productMapper.updateStatusIfLocked(
                product.getId(),
                3);
        if(updated == 0){
            throw new BusinessException("商品状态已发生变化");
        }
        order.setStatus(2);
        order.setUpdateTime(LocalDateTime.now());

        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void cancel(Long id, String username) {

        User user = userMapper.findByUsername(username);

        if (user == null) {
            throw new UnauthorizedException("用户不存在");
        }

        Order order = orderMapper.selectById(id);

        if (order == null) {
            throw new NotFoundException("订单不存在");
        }

        if (!order.getBuyerId().equals(user.getId())) {
            throw new ForbiddenException("只有买家可以取消订单");
        }

        if (!Integer.valueOf(1).equals(order.getStatus())) {
            throw new BusinessException("当前订单不能取消");
        }

        int updated = productMapper.releaseLockedProduct(order.getProductId(),1);
        if(updated == 0){
            throw new BusinessException("商品状态已发生变化");
        }
        order.setStatus(4);
        order.setUpdateTime(LocalDateTime.now());

        orderMapper.updateById(order);
    }
}