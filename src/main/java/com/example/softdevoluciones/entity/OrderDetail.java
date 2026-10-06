package com.example.softdevoluciones.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_details")
@Getter
@Setter
@NoArgsConstructor
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "El pedido asociado es obligatorio. Por favor, verifica la información e inténtalo de nuevo.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @NotBlank(message = "El nombre del producto es obligatorio. Por favor, verifica la información del pedido e inténtalo de nuevo.")
    @Size(max = 200, message = "El nombre del producto no puede superar los 200 caracteres. Por favor, verifica la información e inténtalo de nuevo.")
    @Column(nullable = false, length = 200)
    private String productName;

    @NotNull(message = "La cantidad es obligatoria. Por favor, indica cuántas unidades corresponden a este producto.")
    @Min(value = 1, message = "La cantidad debe ser al menos 1. Por favor, ingresa un valor válido e inténtalo de nuevo.")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "El precio unitario es obligatorio. Por favor, verifica la información del producto e inténtalo de nuevo.")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio unitario debe ser mayor a 0. Por favor, verifica la información e inténtalo de nuevo.")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @NotNull(message = "El subtotal es obligatorio. Por favor, verifica las cantidades e inténtalo de nuevo.")
    @DecimalMin(value = "0.0", inclusive = true, message = "El subtotal no puede ser negativo. Por favor, verifica las cantidades e inténtalo de nuevo.")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
