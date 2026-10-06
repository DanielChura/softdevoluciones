package com.example.softdevoluciones.mapper;

import java.util.List;

import com.example.softdevoluciones.dto.response.ReturnDetailResponse;
import com.example.softdevoluciones.dto.response.ReturnResponse;
import com.example.softdevoluciones.entity.ReturnDetail;
import com.example.softdevoluciones.entity.ReturnRequest;

public final class ReturnMapper {

    private ReturnMapper() {
    }

    public static ReturnResponse toResponse(ReturnRequest request) {
        ReturnResponse response = new ReturnResponse();
        response.setId(request.getId());
        if (request.getOrder() != null) {
            response.setOrderId(request.getOrder().getId());
        }
        if (request.getUser() != null) {
            response.setUserId(request.getUser().getId());
        }
        response.setStatus(request.getStatus());
        response.setReason(request.getReason());
        response.setComment(request.getComment());
        response.setOperatorNote(request.getOperatorNote());
        response.setAmount(request.getAmount());
        response.setCreatedAt(request.getCreatedAt());

        if (request.getItems() != null) {
            List<ReturnDetailResponse> itemResponses = request.getItems()
                    .stream()
                    .map(ReturnMapper::toItemResponse)
                    .toList();
            response.setItems(itemResponses);
        }

        return response;
    }

    public static ReturnDetailResponse toItemResponse(ReturnDetail item) {
        ReturnDetailResponse response = new ReturnDetailResponse();
        response.setId(item.getId());
        if (item.getOrderDetail() != null) {
            var orderDetail = item.getOrderDetail();
            response.setOrderDetailId(orderDetail.getId());
            response.setProductName(orderDetail.getProductName());
            response.setUnitPrice(orderDetail.getUnitPrice());
            if (orderDetail.getProduct() != null) {
                response.setProductId(orderDetail.getProduct().getId());
                response.setImageUrl(orderDetail.getProduct().getImageUrl());
            }
            response.setOrderDetail(OrderMapper.toItemResponse(orderDetail));
        }
        response.setQuantity(item.getQuantity());
        response.setAmount(item.getAmount());
        return response;
    }
}
