package com.tcxw.controller;

import com.tcxw.dto.OrderCreateRequest;
import com.tcxw.dto.OrderResponse;
import com.tcxw.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse create(@RequestBody @Valid OrderCreateRequest body,
                                HttpServletRequest request) {
        return orderService.create(body, username(request));
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id, HttpServletRequest request) {
        return orderService.getById(id, username(request));
    }

    @GetMapping("/my")
    public List<OrderResponse> getMyOrders(HttpServletRequest request) {
        return orderService.getMyOrders(username(request));
    }

    @PutMapping("/{id}/pay")
    public Map<String, String> pay(@PathVariable Long id, HttpServletRequest request) {
        orderService.pay(id, username(request));
        return Map.of("message", "订单支付成功");
    }

    @PutMapping("/{id}/cancel")
    public Map<String, String> cancel(@PathVariable Long id, HttpServletRequest request) {
        orderService.cancel(id, username(request));
        return Map.of("message", "订单取消成功");
    }

    private String username(HttpServletRequest request) {
        return (String) request.getAttribute("username");
    }
}
