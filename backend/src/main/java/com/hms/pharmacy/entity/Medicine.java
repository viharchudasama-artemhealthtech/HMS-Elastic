package com.hms.pharmacy.entity;

import com.hms.common.entity.Auditable;
import com.hms.common.enums.MedicineCategory;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "medicines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Medicine extends Auditable {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String medicineCode;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private MedicineCategory category;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "quantity_in_stock", nullable = false)
    private Integer quantityInStock;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "reorder_level")
    private Integer reorderLevel;

    @Column(name = "dosage")
    private String dosage;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

}
