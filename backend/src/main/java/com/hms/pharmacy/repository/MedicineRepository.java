package com.hms.pharmacy.repository;

import com.hms.common.enums.MedicineCategory;
import com.hms.pharmacy.entity.Medicine;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    boolean existsByMedicineCode(String medicineCode);

    List<Medicine> findByIsActiveTrue();

    Slice<Medicine> findAllBy(Pageable pageable);

    long countByQuantityInStockGreaterThan(int quantity);

    long countByQuantityInStockLessThanEqual(int quantity);

    @Query("""
            SELECT m FROM Medicine m
            WHERE m.isActive = true
              AND (
                    LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(COALESCE(m.manufacturer, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(COALESCE(m.medicineCode, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY m.name ASC
            """)
    List<Medicine> searchActiveMedicines(@Param("keyword") String keyword, Pageable pageable);

    List<Medicine> findByCategory(MedicineCategory category);

    Optional<Medicine> findByNameIgnoreCase(String name);

    @Modifying
    @Query("UPDATE Medicine m SET m.quantityInStock = m.quantityInStock - :qty " +
           "WHERE m.id = :id AND m.quantityInStock >= :qty")
    int deductStockAtomic(@Param("id") Long id, @Param("qty") Integer qty);

    @Modifying
    @Query("UPDATE Medicine m SET m.quantityInStock = m.quantityInStock + :qty " +
           "WHERE m.id = :id")
    void addStockAtomic(@Param("id") Long id, @Param("qty") Integer qty);

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.quantityInStock <= m.reorderLevel OR (m.reorderLevel IS NULL AND m.quantityInStock <= 10)")
    long countLowStock();
}
