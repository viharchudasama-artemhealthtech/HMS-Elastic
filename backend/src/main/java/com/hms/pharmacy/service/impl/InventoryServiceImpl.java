package com.hms.pharmacy.service.impl;

import com.hms.common.audit.AuditLogService;
import com.hms.pharmacy.dto.request.DispenseMedicineRequestDTO;
import com.hms.pharmacy.dto.response.InventoryTransactionResponseDTO;
import com.hms.pharmacy.entity.InventoryTransaction;
import com.hms.pharmacy.entity.Medicine;
import com.hms.pharmacy.exception.InsufficientStockException;
import com.hms.pharmacy.exception.MedicineNotFoundException;
import com.hms.pharmacy.repository.InventoryTransactionRepository;
import com.hms.pharmacy.repository.MedicineRepository;
import com.hms.pharmacy.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final MedicineRepository medicineRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public void recordInitialStock(Medicine medicine) {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .medicine(medicine)
                .transactionType("IN")
                .quantity(medicine.getQuantityInStock())
                .referenceId(medicine.getId())
                .notes("Initial stock at creation")
                .build();
        inventoryTransactionRepository.save(transaction);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dispenseMedicines(DispenseMedicineRequestDTO request) {
        for (DispenseMedicineRequestDTO.DispenseItemDTO item : request.getItems()) {
            Medicine medicine = medicineRepository.findById(item.getMedicineId())
                    .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with ID: " + item.getMedicineId()));

            int updatedRows = medicineRepository.deductStockAtomic(medicine.getId(), item.getQuantity());
            if (updatedRows == 0) {
                throw new InsufficientStockException(medicine.getName(), "Insufficient stock for medicine: " + medicine.getName());
            }

            InventoryTransaction transaction = InventoryTransaction.builder()
                    .medicine(medicine)
                    .transactionType("OUT")
                    .quantity(item.getQuantity())
                    .referenceId(request.getPrescriptionId())
                    .notes("Dispensed against manual request")
                    .build();

            inventoryTransactionRepository.save(transaction);
            auditLogService.log(getCurrentUsername(), "MEDICINE_DISPENSE", "Medicine", item.getMedicineId().toString(), "qty=" + item.getQuantity());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restockMedicine(Long id, Integer quantity) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with ID: " + id));

        medicineRepository.addStockAtomic(medicine.getId(), quantity);

        InventoryTransaction transaction = InventoryTransaction.builder()
                .medicine(medicine)
                .transactionType("IN")
                .quantity(quantity)
                .referenceId(id)
                .notes("Manual Restock")
                .build();

        inventoryTransactionRepository.save(transaction);
        auditLogService.log(getCurrentUsername(), "MEDICINE_RESTOCK", "Medicine", id.toString(), "qty=" + quantity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransactionResponseDTO> getAllTransactions() {
        return inventoryTransactionRepository.findAll().stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .map(t -> InventoryTransactionResponseDTO.builder()
                        .id(t.getId())
                        .medicineId(t.getMedicine().getId())
                        .medicineName(t.getMedicine().getName())
                        .medicineCode(t.getMedicine().getMedicineCode())
                        .transactionType(t.getTransactionType())
                        .quantity(t.getQuantity())
                        .referenceId(t.getReferenceId())
                        .notes(t.getNotes())
                        .createdAt(t.getCreatedAt())
                        .createdBy(t.getCreatedBy())
                        .build())
                .toList();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }
}
