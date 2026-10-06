package com.example.softdevoluciones.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.softdevoluciones.dto.request.ReturnCreateRequest;
import com.example.softdevoluciones.dto.request.ReturnStatusRequest;
import com.example.softdevoluciones.dto.response.ReturnResponse;
import com.example.softdevoluciones.entity.User;
import com.example.softdevoluciones.service.ReturnRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnRequestController {

    private final ReturnRequestService returnRequestService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public Page<ReturnResponse> findAll(
            @PageableDefault(size = 10, sort = { "createdAt" }, direction = Sort.Direction.DESC) Pageable pageable) {
        return returnRequestService.findAll(pageable);
    }

    @GetMapping("/my-returns")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'OPERATOR')")
    public Page<ReturnResponse> findAllByUser(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = { "createdAt" }, direction = Sort.Direction.DESC) Pageable pageable) {
        return returnRequestService.findAllByUser(user, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'OPERATOR')")
    public ReturnResponse findById(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return returnRequestService.findById(id, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'OPERATOR')")
    public ReturnResponse create(@AuthenticationPrincipal User user, @Valid @RequestBody ReturnCreateRequest request) {
        return returnRequestService.create(request, user);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ReturnResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ReturnStatusRequest request) {
        return returnRequestService.updateStatus(id, request);
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'OPERATOR')")
    public ReturnResponse complete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return returnRequestService.complete(id, user);
    }
}
