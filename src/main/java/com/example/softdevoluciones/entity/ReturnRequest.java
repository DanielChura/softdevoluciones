package com.example.softdevoluciones.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.softdevoluciones.enums.ReturnStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "return_requests")
@Getter
@Setter
@NoArgsConstructor
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "El estado de la devolución es obligatorio. Por favor, indica el estado correspondiente para continuar.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReturnStatus status = ReturnStatus.REQUESTED;

    @NotBlank(message = "El motivo es obligatorio. Por favor, describe el motivo de la devolución para continuar.")
    @Size(max = 500, message = "El motivo no puede superar los 500 caracteres. Por favor, acorta el texto e inténtalo de nuevo.")
    @Column(nullable = false, length = 500)
    private String reason;

    @Size(max = 500, message = "El comentario no puede superar los 500 caracteres. Por favor, acorta el texto e inténtalo de nuevo.")
    @Column(length = 500)
    private String comment;

    @Size(max = 500, message = "La nota del operador no puede superar los 500 caracteres. Por favor, acorta el texto e inténtalo de nuevo.")
    @Column(length = 500)
    private String operatorNote;

    @NotNull(message = "El importe de la devolución es obligatorio. Por favor, verifica los productos e inténtalo de nuevo.")
    @DecimalMin(value = "0.0", inclusive = true, message = "El importe de la devolución no puede ser negativo. Por favor, verifica los productos e inténtalo de nuevo.")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @NotNull(message = "La compra asociada es obligatoria. Por favor, indica el pedido sobre el cual deseas solicitar la devolución.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Order order;

    @NotNull(message = "El usuario solicitante es obligatorio. Por favor, verifica la sesión e inténtalo de nuevo.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnDetail> items = new ArrayList<>();

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
