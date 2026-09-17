package com.tcxw.service;

import com.tcxw.consumer.OrderTimeoutConsumer;
import com.tcxw.dto.OrderCreateRequest;
import com.tcxw.dto.OrderResponse;
import com.tcxw.entity.Order;
import com.tcxw.entity.Product;
import com.tcxw.entity.User;
import com.tcxw.enums.OrderStatus;
import com.tcxw.enums.ProductStatus;
import com.tcxw.exception.BusinessException;
import com.tcxw.mapper.OrderMapper;
import com.tcxw.mapper.ProductMapper;
import com.tcxw.mapper.UserMapper;
import com.tcxw.producer.OrderMessageProducer;
import com.tcxw.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderConcurrencyTest {

    @Test
    void onlyOneBuyerCanLockTheSameProduct() throws Exception {
        OrderMapper orderMapper = mock(OrderMapper.class);
        ProductMapper productMapper = mock(ProductMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        OrderMessageProducer producer = mock(OrderMessageProducer.class);
        OrderServiceImpl service = new OrderServiceImpl(orderMapper, productMapper, userMapper, producer);

        User buyer = user(2L, "buyer");
        Product product = product(10L, 1L);
        when(userMapper.findByUsername("buyer")).thenReturn(buyer);
        when(productMapper.selectById(10L)).thenReturn(product);

        AtomicBoolean locked = new AtomicBoolean(false);
        when(productMapper.updateStatusIfAvailable(10L, ProductStatus.LOCKED.getCode()))
                .thenAnswer(invocation -> locked.compareAndSet(false, true) ? 1 : 0);
        AtomicLong orderId = new AtomicLong();
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(orderId.incrementAndGet());
            return 1;
        }).when(orderMapper).insert(any(Order.class));

        int buyers = 100;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch start = new CountDownLatch(1);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < buyers; i++) {
            tasks.add(() -> {
                start.await();
                try {
                    OrderCreateRequest request = new OrderCreateRequest();
                    request.setProductId(10L);
                    OrderResponse ignored = service.create(request, "buyer");
                    return true;
                } catch (BusinessException exception) {
                    return false;
                }
            });
        }

        List<Future<Boolean>> futures = tasks.stream().map(executor::submit).toList();
        start.countDown();
        int successes = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) successes++;
        }
        executor.shutdownNow();

        assertEquals(1, successes);
        assertEquals(1, orderId.get());
    }

    @Test
    void paymentAndTimeoutEndInOnlyOneLegalState() throws Exception {
        OrderMapper orderMapper = mock(OrderMapper.class);
        ProductMapper productMapper = mock(ProductMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        OrderMessageProducer producer = mock(OrderMessageProducer.class);
        OrderServiceImpl service = new OrderServiceImpl(orderMapper, productMapper, userMapper, producer);
        OrderTimeoutConsumer consumer = new OrderTimeoutConsumer(orderMapper, productMapper);

        User buyer = user(2L, "buyer");
        Order order = order(20L, buyer.getId(), 10L);
        when(userMapper.findByUsername("buyer")).thenReturn(buyer);
        when(orderMapper.selectById(20L)).thenReturn(order);

        AtomicInteger orderState = new AtomicInteger(OrderStatus.PENDING_PAYMENT.getCode());
        AtomicInteger productState = new AtomicInteger(ProductStatus.LOCKED.getCode());
        when(orderMapper.markPaidIfPending(20L)).thenAnswer(invocation ->
                orderState.compareAndSet(OrderStatus.PENDING_PAYMENT.getCode(), OrderStatus.PAID.getCode()) ? 1 : 0);
        when(orderMapper.cancelIfPending(20L)).thenAnswer(invocation ->
                orderState.compareAndSet(OrderStatus.PENDING_PAYMENT.getCode(), OrderStatus.CANCELLED.getCode()) ? 1 : 0);
        when(productMapper.updateStatusIfLocked(10L, ProductStatus.SOLD.getCode())).thenAnswer(invocation ->
                productState.compareAndSet(ProductStatus.LOCKED.getCode(), ProductStatus.SOLD.getCode()) ? 1 : 0);
        when(productMapper.releaseLockedProduct(10L, ProductStatus.AVAILABLE.getCode())).thenAnswer(invocation ->
                productState.compareAndSet(ProductStatus.LOCKED.getCode(), ProductStatus.AVAILABLE.getCode()) ? 1 : 0);

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<?> payment = executor.submit(() -> {
            await(start);
            try {
                service.pay(20L, "buyer");
            } catch (BusinessException ignored) {
                // Timeout won the conditional order update.
            }
        });
        Future<?> timeout = executor.submit(() -> {
            await(start);
            consumer.handleTimeout(new com.tcxw.message.OrderMessage(20L, buyer.getId(), "ORDER_CREATED"));
        });
        start.countDown();
        payment.get();
        timeout.get();
        executor.shutdownNow();

        boolean paid = orderState.get() == OrderStatus.PAID.getCode()
                && productState.get() == ProductStatus.SOLD.getCode();
        boolean cancelled = orderState.get() == OrderStatus.CANCELLED.getCode()
                && productState.get() == ProductStatus.AVAILABLE.getCode();
        assertTrue(paid || cancelled);
    }

    @Test
    void duplicateTimeoutMessageReleasesProductOnlyOnce() {
        OrderMapper orderMapper = mock(OrderMapper.class);
        ProductMapper productMapper = mock(ProductMapper.class);
        OrderTimeoutConsumer consumer = new OrderTimeoutConsumer(orderMapper, productMapper);
        Order order = order(20L, 2L, 10L);
        when(orderMapper.selectById(20L)).thenReturn(order);
        when(orderMapper.cancelIfPending(20L)).thenReturn(1, 0);
        when(productMapper.releaseLockedProduct(10L, ProductStatus.AVAILABLE.getCode())).thenReturn(1);

        var message = new com.tcxw.message.OrderMessage(20L, 2L, "ORDER_CREATED");
        consumer.handleTimeout(message);
        consumer.handleTimeout(message);

        verify(productMapper).releaseLockedProduct(10L, ProductStatus.AVAILABLE.getCode());
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole("USER");
        user.setStatus(1);
        return user;
    }

    private static Product product(Long id, Long sellerId) {
        Product product = new Product();
        product.setId(id);
        product.setUserId(sellerId);
        product.setPrice(new BigDecimal("99.00"));
        product.setStatus(ProductStatus.AVAILABLE.getCode());
        return product;
    }

    private static Order order(Long id, Long buyerId, Long productId) {
        Order order = new Order();
        order.setId(id);
        order.setBuyerId(buyerId);
        order.setProductId(productId);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        return order;
    }
}
