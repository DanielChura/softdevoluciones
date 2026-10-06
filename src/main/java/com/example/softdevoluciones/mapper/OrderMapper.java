package com.example.softdevoluciones.mapper;

import java.util.List;

import com.example.softdevoluciones.dto.response.OrderItemResponse;
import com.example.softdevoluciones.dto.response.OrderResponse;
import com.example.softdevoluciones.entity.Order;
import com.example.softdevoluciones.entity.OrderDetail;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        if (order.getUser() != null) {
            response.setUserId(order.getUser().getId());
        }
        response.setPhone(order.getPhone());
        response.setAddress(order.getAddress());
        response.setStatus(order.getStatus());
        response.setTotal(order.getTotal());
        response.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(item -> toItemResponse(item))
                .toList();

        response.setItems(itemResponses);
        return response;
    }

    public static OrderItemResponse toItemResponse(OrderDetail item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        if (item.getProduct() != null) {
            response.setProductId(item.getProduct().getId());
            response.setImageUrl(item.getProduct().getImageUrl());
        }
        response.setProductName(item.getProductName());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setSubtotal(item.getSubtotal());
        return response;
    }
}
