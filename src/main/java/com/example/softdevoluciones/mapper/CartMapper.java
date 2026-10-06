package com.example.softdevoluciones.mapper;

import java.math.BigDecimal;
import java.util.List;

import com.example.softdevoluciones.dto.response.CartItemResponse;
import com.example.softdevoluciones.dto.response.CartResponse;
import com.example.softdevoluciones.entity.Cart;
import com.example.softdevoluciones.entity.CartItem;

public final class CartMapper {

    private CartMapper() {
    }

    public static CartResponse toResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        if (cart.getUser() != null) {
            response.setUserId(cart.getUser().getId());
        }
        List<CartItemResponse> items = cart.getItems().stream()
                .map(CartMapper::toItemResponse)
                .toList();
        response.setItems(items);
        response.setTotal(items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return response;
    }

    public static CartItemResponse toItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setImageUrl(item.getProduct().getImageUrl());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getProduct().getPrice());
        response.setSubtotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        return response;
    }
}
