package com.tcxw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tcxw.dto.OrderCreateRequest;
import com.tcxw.dto.OrderResponse;
import com.tcxw.entity.Order;
import com.tcxw.entity.Product;
import com.tcxw.entity.User;
import com.tcxw.enums.OrderStatus;
import com.tcxw.enums.ProductStatus;
import com.tcxw.exception.BusinessException;
import com.tcxw.exception.ForbiddenException;
import com.tcxw.exception.NotFoundException;
import com.tcxw.exception.UnauthorizedException;
import com.tcxw.mapper.OrderMapper;
import com.tcxw.mapper.ProductMapper;
import com.tcxw.mapper.UserMapper;
import com.tcxw.message.OrderMessage;
import com.tcxw.producer.OrderMessageProducer;
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
    public OrderResponse create(OrderCreateRequest request, String username) {
        User buyer = requireUser(username);
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new NotFoundException("商品不存在");
        }
        if (product.getUserId().equals(buyer.getId())) {
            throw new BusinessException("不能购买自己的商品");
        }

        int productUpdated = productMapper.updateStatusIfAvailable(
                product.getId(), ProductStatus.LOCKED.getCode());
        if (productUpdated == 0) {
            throw new BusinessException("商品已被其他用户锁定或不可购买");
        }

        LocalDateTime now = LocalDateTime.now();
        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setBuyerId(buyer.getId());
        order.setSellerId(product.getUserId());
        order.setProductId(product.getId());
        order.setPrice(product.getPrice());
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        orderMapper.insert(order);

        orderMessageProducer.sendAfterCommit(new OrderMessage(order.getId(), buyer.getId(), "ORDER_CREATED"));
        return OrderResponse.from(order);
    }

    @Override
    public OrderResponse getById(Long id, String username) {
        User user = requireUser(username);
        Order order = requireOrder(id);
        if (!"ADMIN".equals(user.getRole())
                && !order.getBuyerId().equals(user.getId())
                && !order.getSellerId().equals(user.getId())) {
            throw new ForbiddenException("无权查看该订单");
        }
        return OrderResponse.from(order);
    }

    @Override
    public List<OrderResponse> getMyOrders(String username) {
        User user = requireUser(username);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(query -> query.eq(Order::getBuyerId, user.getId())
                        .or().eq(Order::getSellerId, user.getId()))
                .orderByDesc(Order::getCreateTime);
        return orderMapper.selectList(wrapper).stream().map(OrderResponse::from).toList();
    }

    @Override
    @Transactional
    public void pay(Long id, String username) {
        User user = requireUser(username);
        Order order = requireOrder(id);
        if (!order.getBuyerId().equals(user.getId())) {
            throw new ForbiddenException("只有买家可以支付订单");
        }

        // Order first, product second. Cancel and timeout use the same lock order.
        if (orderMapper.markPaidIfPending(id) == 0) {
            throw new BusinessException("当前订单不能支付");
        }
        if (productMapper.updateStatusIfLocked(order.getProductId(), ProductStatus.SOLD.getCode()) == 0) {
            throw new BusinessException("商品状态已发生变化");
        }
    }

    @Override
    @Transactional
    public void cancel(Long id, String username) {
        User user = requireUser(username);
        Order order = requireOrder(id);
        if (!order.getBuyerId().equals(user.getId())) {
            throw new ForbiddenException("只有买家可以取消订单");
        }

        if (orderMapper.cancelIfPending(id) == 0) {
            throw new BusinessException("当前订单不能取消");
        }
        if (productMapper.releaseLockedProduct(order.getProductId(), ProductStatus.AVAILABLE.getCode()) == 0) {
            throw new BusinessException("商品状态已发生变化");
        }
    }

    private User requireUser(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UnauthorizedException("用户不存在");
        }
        return user;
    }

    private Order requireOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new NotFoundException("订单不存在");
        }
        return order;
    }
}
