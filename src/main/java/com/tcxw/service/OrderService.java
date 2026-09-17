package com.tcxw.service;

import com.tcxw.dto.OrderCreateRequest;
import com.tcxw.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse create(OrderCreateRequest request, String username);

    OrderResponse getById(Long id, String username);

    List<OrderResponse> getMyOrders(String username);

    void pay(Long id, String username);

    void cancel(Long id, String username);
}
