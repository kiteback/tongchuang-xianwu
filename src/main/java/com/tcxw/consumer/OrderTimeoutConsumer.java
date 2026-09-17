package com.tcxw.consumer;

import com.tcxw.config.RabbitMQConfig;
import com.tcxw.entity.Order;
import com.tcxw.enums.ProductStatus;
import com.tcxw.mapper.OrderMapper;
import com.tcxw.mapper.ProductMapper;
import com.tcxw.message.OrderMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderTimeoutConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutConsumer.class);

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    public OrderTimeoutConsumer(OrderMapper orderMapper, ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_TIMEOUT_HANDLER_QUEUE)
    @Transactional
    public void handleTimeout(OrderMessage message) {
        Order order = orderMapper.selectById(message.getOrderId());
        if (order == null) {
            log.warn("Ignoring timeout event {} because order {} does not exist",
                    message.getEventId(), message.getOrderId());
            return;
        }

        // All terminal order operations lock/update the order row first, then the product row.
        int orderUpdated = orderMapper.cancelIfPending(order.getId());
        if (orderUpdated == 0) {
            log.info("Ignoring duplicate or obsolete timeout event {} for order {}",
                    message.getEventId(), order.getId());
            return;
        }

        int productUpdated = productMapper.releaseLockedProduct(
                order.getProductId(), ProductStatus.AVAILABLE.getCode());
        if (productUpdated == 0) {
            throw new IllegalStateException("订单已取消但商品释放失败，事务将回滚");
        }
        log.info("Cancelled timed-out order {} and released product {}", order.getId(), order.getProductId());
    }
}
