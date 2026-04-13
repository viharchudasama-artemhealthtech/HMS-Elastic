package com.hms.pharmacy.service;

import com.hms.pharmacy.dto.request.MedicineRequestDTO;
import com.hms.pharmacy.dto.response.MedicineResponseDTO;
import com.hms.pharmacy.dto.response.MedicineSliceResponseDTO;

import java.util.List;


public interface MedicineService {

    MedicineResponseDTO createMedicine(MedicineRequestDTO dto);

    MedicineResponseDTO updateMedicine(Long id, MedicineRequestDTO dto);

    void deleteMedicine(Long id);

    MedicineResponseDTO getMedicineById(Long id);

    List<MedicineResponseDTO> getAllMedicines();

    MedicineSliceResponseDTO getMedicinesSlice(int page, int size);

    List<MedicineResponseDTO> getMedicinesByCategory(String category);

    boolean existsByMedicineCode(String medicineCode);
}
