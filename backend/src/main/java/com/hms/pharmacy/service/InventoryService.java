package com.hms.pharmacy.service;

import com.hms.pharmacy.dto.request.DispenseMedicineRequestDTO;
import com.hms.pharmacy.dto.response.InventoryTransactionResponseDTO;
import com.hms.pharmacy.entity.Medicine;

import java.util.List;

public interface InventoryService {

    void recordInitialStock(Medicine medicine);

    void dispenseMedicines(DispenseMedicineRequestDTO request);

    void restockMedicine(Long id, Integer quantity);

    List<InventoryTransactionResponseDTO> getAllTransactions();
}
