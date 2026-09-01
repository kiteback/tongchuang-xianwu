package com.tcxw.controller;

import com.tcxw.dto.OrderCreateRequest;
import com.tcxw.entity.Order;
import com.tcxw.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Order create(
            @RequestBody @Valid OrderCreateRequest request,
            HttpServletRequest httpRequest) {

        String username = (String) httpRequest.getAttribute("username");

        return orderService.create(request, username);
    }

    @GetMapping("/{id}")
    public Order getById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        String username = (String) httpRequest.getAttribute("username");

        return orderService.getById(id, username);
    }

    @GetMapping("/my")
    public List<Order> getMyOrders(HttpServletRequest httpRequest) {

        String username = (String) httpRequest.getAttribute("username");

        return orderService.getMyOrders(username);
    }

    @PutMapping("/{id}/pay")
    public String pay(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        String username = (String) httpRequest.getAttribute("username");

        orderService.pay(id, username);

        return "订单支付成功";
    }

    @PutMapping("/{id}/cancel")
    public String cancel(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        String username = (String) httpRequest.getAttribute("username");

        orderService.cancel(id, username);

        return "订单取消成功";
    }
}