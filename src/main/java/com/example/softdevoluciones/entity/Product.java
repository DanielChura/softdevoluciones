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
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "El nombre del producto es obligatorio. Por favor, ingresa un nombre para continuar.")
    @Size(max = 200, message = "El nombre del producto no puede superar los 200 caracteres. Por favor, acorta el texto e inténtalo de nuevo.")
    @Column(nullable = false, length = 200)
    private String name;

    @Size(max = 600, message = "La descripción no puede superar los 600 caracteres. Por favor, acorta el texto e inténtalo de nuevo.")
    @Column(length = 600)
    private String description;

    @NotNull(message = "El precio es obligatorio. Por favor, ingresa el precio del producto para continuar.")
    @DecimalMin(value = "0.01", inclusive = true, message = "El precio debe ser mayor a 0. Por favor, ingresa un valor válido e inténtalo de nuevo.")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "El stock es obligatorio. Por favor, indica la cantidad disponible del producto.")
    @Min(value = 0, message = "El stock no puede ser negativo. Por favor, ingresa un valor igual o mayor a 0.")
    @Column(nullable = false)
    private Integer stock;

    @NotBlank(message = "La imagen del producto es obligatoria. Por favor, adjunta una imagen en formato WEBP para continuar.")
    @Size(max = 500, message = "La URL de la imagen no puede superar los 500 caracteres. Por favor, verifica el archivo e inténtalo de nuevo.")
    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Size(max = 255, message = "El identificador público de la imagen no puede superar los 255 caracteres. Por favor, verifica el archivo e inténtalo de nuevo.")
    private String publicId;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Category category;

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
