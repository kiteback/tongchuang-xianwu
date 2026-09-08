package com.tcxw.consumer;

import com.tcxw.entity.Order;
import com.tcxw.mapper.OrderMapper;
import com.tcxw.mapper.ProductMapper;
import com.tcxw.message.OrderMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderTimeoutConsumer {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    @RabbitListener(queues = "order.timeout.handler.queue")
    @Transactional
    public void handleTimeout(OrderMessage message) {

        Order order = orderMapper.selectById(message.getOrderId());

        if (order == null) {
            System.out.println("订单不存在：orderId=" + message.getOrderId());
            return;
        }

        if(!Integer.valueOf(1).equals(order.getStatus())){
            System.out.println(
                    "订单当前无需超时处理：orderId=" + order.getId()
                    + ", status=" + order.getStatus()
            );
            return;
        }

        int orderUpdated = orderMapper.cancelIfPending(order.getId());

        if (orderUpdated == 0) {
            return;
        }

        int productUpdated = productMapper.releaseLockedProduct(
                order.getProductId(),
                1
        );

        if (productUpdated == 0) {
            throw new RuntimeException("商品释放失败");
        }

        System.out.println(
                "订单超时取消成功：orderId=" + order.getId()
                        + ", productId=" + order.getProductId()
        );

    }

    public OrderTimeoutConsumer(OrderMapper orderMapper,
                             ProductMapper productMapper){
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
    }
}
