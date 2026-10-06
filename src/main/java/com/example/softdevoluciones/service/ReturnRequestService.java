package com.example.softdevoluciones.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.softdevoluciones.dto.request.ReturnCreateRequest;
import com.example.softdevoluciones.dto.request.ReturnDetailRequest;
import com.example.softdevoluciones.dto.request.ReturnStatusRequest;
import com.example.softdevoluciones.dto.response.ReturnResponse;
import com.example.softdevoluciones.entity.Order;
import com.example.softdevoluciones.entity.OrderDetail;
import com.example.softdevoluciones.entity.Product;
import com.example.softdevoluciones.entity.ReturnDetail;
import com.example.softdevoluciones.entity.ReturnRequest;
import com.example.softdevoluciones.entity.User;
import com.example.softdevoluciones.enums.ReturnStatus;
import com.example.softdevoluciones.enums.UserRole;
import com.example.softdevoluciones.exception.BadRequestException;
import com.example.softdevoluciones.exception.ResourceNotFoundException;
import com.example.softdevoluciones.mapper.ReturnMapper;
import com.example.softdevoluciones.repository.OrderRepository;
import com.example.softdevoluciones.repository.ProductRepository;
import com.example.softdevoluciones.repository.ReturnDetailRepository;
import com.example.softdevoluciones.repository.ReturnRequestRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReturnRequestService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;
    private final ReturnDetailRepository returnDetailRepository;
    private final ProductRepository productRepository;

    public Page<ReturnResponse> findAll(Pageable pageable) {
        return returnRequestRepository.findAll(pageable)
                .map(ReturnMapper::toResponse);
    }

    public Page<ReturnResponse> findAllByUser(User user, Pageable pageable) {
        return returnRequestRepository.findByUser_Id(user.getId(), pageable)
                .map(ReturnMapper::toResponse);
    }

    @Transactional
    public ReturnResponse findById(UUID id, User user) {
        ReturnRequest returnRequest = getById(id);
        if (user.getRole() == UserRole.CUSTOMER && !returnRequest.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "No tienes permiso para ver esta solicitud de devolución porque pertenece a otro usuario. Si necesitas ayuda, contacta al soporte.");
        }

        if ((user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.OPERATOR)
                && returnRequest.getStatus() == ReturnStatus.REQUESTED) {
            returnRequest.setStatus(ReturnStatus.IN_REVIEW);
            returnRequest = returnRequestRepository.save(returnRequest);
        }

        return ReturnMapper.toResponse(returnRequest);
    }

    @Transactional
    public ReturnResponse create(ReturnCreateRequest request, User user) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La compra asociada a esta devolución no existe o fue eliminada. Por favor, verifica la información e inténtalo de nuevo."));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "No tienes permiso para solicitar una devolución sobre este pedido porque pertenece a otro usuario. Verifica que hayas iniciado sesión con la cuenta correcta.");
        }

        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setUser(user);
        returnRequest.setStatus(ReturnStatus.REQUESTED);
        returnRequest.setReason(request.getReason());
        returnRequest.setComment(request.getComment());
        returnRequest.setOrder(order);

        List<ReturnDetail> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ReturnDetailRequest itemReq : request.getItems()) {
            OrderDetail orderDetail = order.getItems().stream()
                    .filter(item -> item.getId().equals(itemReq.getOrderDetailId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException(
                            "Uno de los productos indicados no pertenece a la compra seleccionada. Por favor, revisa el detalle de tu pedido y vuelve a intentarlo."));

            Long returnedQuantity = returnDetailRepository.findApprovedQuantityByOrderDetailId(orderDetail.getId());
            int alreadyReturned = returnedQuantity != null ? returnedQuantity.intValue() : 0;
            int availableToReturn = orderDetail.getQuantity() - alreadyReturned;

            if (itemReq.getQuantity() > availableToReturn) {
                throw new BadRequestException(
                        "La cantidad solicitada para la devolución del producto '" + orderDetail.getProductName()
                                + "' no es válida. Cantidad máxima que puedes devolver: " + availableToReturn
                                + ". Por favor, ajusta la cantidad e inténtalo de nuevo.");
            }

            BigDecimal amount = orderDetail.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(amount);

            ReturnDetail returnDetail = new ReturnDetail();
            returnDetail.setRequest(returnRequest);
            returnDetail.setOrderDetail(orderDetail);
            returnDetail.setQuantity(itemReq.getQuantity());
            returnDetail.setAmount(amount);

            items.add(returnDetail);
        }

        returnRequest.setItems(items);
        returnRequest.setAmount(totalAmount);

        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        return ReturnMapper.toResponse(saved);
    }

    @Transactional
    public ReturnResponse updateStatus(UUID id, ReturnStatusRequest req) {
        ReturnRequest returnRequest = getById(id);
        ReturnStatus currentStatus = returnRequest.getStatus();
        ReturnStatus nextStatus = req.getStatus();

        validateStateTransition(currentStatus, nextStatus);

        if (nextStatus == ReturnStatus.APPROVED && currentStatus != ReturnStatus.APPROVED) {
            for (ReturnDetail item : returnRequest.getItems()) {
                Product product = item.getOrderDetail().getProduct();
                if (product != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        returnRequest.setStatus(nextStatus);
        returnRequest.setOperatorNote(req.getOperatorNote());

        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        return ReturnMapper.toResponse(saved);
    }

    @Transactional
    public ReturnResponse complete(UUID id, User user) {
        ReturnRequest returnRequest = getById(id);

        if (user.getRole() == UserRole.CUSTOMER && !returnRequest.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "No tienes permiso para completar esta solicitud de devolución porque pertenece a otro usuario.");
        }

        validateStateTransition(returnRequest.getStatus(), ReturnStatus.COMPLETED);

        returnRequest.setStatus(ReturnStatus.COMPLETED);
        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        return ReturnMapper.toResponse(saved);
    }

    private void validateStateTransition(ReturnStatus current, ReturnStatus next) {
        if (current == next) {
            throw new BadRequestException(
                    "La solicitud de devolución ya se encuentra en estado " + current + ".");
        }

        boolean isValid = switch (current) {
            case REQUESTED -> next == ReturnStatus.IN_REVIEW;
            case IN_REVIEW -> next == ReturnStatus.APPROVED || next == ReturnStatus.REJECTED;
            case APPROVED -> next == ReturnStatus.COMPLETED;
            case REJECTED, COMPLETED -> false;
        };

        if (!isValid) {
            throw new BadRequestException("No es posible cambiar el estado de la solicitud de devolución de " + current
                    + " a " + next
                    + ". Por favor, respeta el flujo permitido: SOLICITADO (REQUESTED) → EN REVISIÓN (IN_REVIEW) → APROBADO (APPROVED) / RECHAZADO (REJECTED) → COMPLETADO (COMPLETED).");
        }
    }

    @Transactional(readOnly = true)
    public ReturnRequest getById(UUID id) {
        return returnRequestRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "La solicitud de devolución indicada no existe o fue eliminada. Por favor, verifica la información e inténtalo de nuevo."));
    }
}
