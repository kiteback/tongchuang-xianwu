package com.tcxw.service;

import com.tcxw.dto.OrderCreateRequest;
import com.tcxw.entity.Order;

import java.util.List;

public interface OrderService {

    Order create(OrderCreateRequest request, String username);

    Order getById(Long id, String username);

    List<Order> getMyOrders(String username);

    void pay(Long id, String username);

    void cancel(Long id, String username);
}