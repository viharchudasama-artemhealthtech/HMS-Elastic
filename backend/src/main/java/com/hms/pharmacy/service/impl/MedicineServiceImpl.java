package com.hms.pharmacy.service.impl;

import com.hms.common.audit.AuditLogService;
import com.hms.common.enums.MedicineCategory;
import com.hms.pharmacy.dto.request.MedicineRequestDTO;
import com.hms.pharmacy.dto.response.MedicineResponseDTO;
import com.hms.pharmacy.dto.response.MedicineSliceResponseDTO;
import com.hms.pharmacy.entity.Medicine;
import com.hms.pharmacy.exception.DuplicateMedicineException;
import com.hms.pharmacy.exception.MedicineNotFoundException;
import com.hms.pharmacy.mapper.MedicineMapper;
import com.hms.pharmacy.repository.MedicineRepository;
import com.hms.pharmacy.service.InventoryService;
import com.hms.pharmacy.service.MedicineService;
import com.hms.pharmacy.service.search.MedicineSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineMapper medicineMapper;
    private final InventoryService inventoryService;
    private final AuditLogService auditLogService;
    private final MedicineSearchService medicineSearchService;

    @Override
    @Transactional
    public MedicineResponseDTO createMedicine(MedicineRequestDTO dto) {

        if (medicineRepository.existsByMedicineCode(dto.getMedicineCode())) {
            throw new DuplicateMedicineException("Medicine code already exists: " + dto.getMedicineCode());
        }

        Medicine savedMedicine = medicineRepository
                .save(medicineMapper.toEntity(dto));

        inventoryService.recordInitialStock(savedMedicine);

        auditLogService.log(getCurrentUsername(), "MEDICINE_CREATE", "Medicine", savedMedicine.getId().toString(), "name=" + savedMedicine.getName());
        medicineSearchService.indexMedicine(savedMedicine);
        return medicineMapper.toDto(savedMedicine);
    }

    @Override
    @Transactional
    public MedicineResponseDTO updateMedicine(Long id, MedicineRequestDTO dto) {
        Medicine existingMedicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with ID: " + id));

        if (!existingMedicine.getMedicineCode().equals(dto.getMedicineCode())
                && medicineRepository.existsByMedicineCode(dto.getMedicineCode())) {
            throw new DuplicateMedicineException("Medicine code already exists: " + dto.getMedicineCode());
        }

        medicineMapper.updateEntityFromDto(dto, existingMedicine);
        Medicine updatedMedicine = medicineRepository.save(existingMedicine);
        auditLogService.log(getCurrentUsername(), "MEDICINE_UPDATE", "Medicine", id.toString(), "name=" + updatedMedicine.getName());
        medicineSearchService.indexMedicine(updatedMedicine);
        return medicineMapper.toDto(updatedMedicine);
    }

    @Override
    @Transactional
    public void deleteMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with ID: " + id));

        medicineRepository.delete(medicine);
        auditLogService.log(getCurrentUsername(), "MEDICINE_DELETE", "Medicine", id.toString(), "name=" + medicine.getName());
        medicineSearchService.deleteMedicineFromIndex(id);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineResponseDTO getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with ID: " + id));

        return medicineMapper.toDto(medicine);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> getAllMedicines() {
        return medicineMapper.toDtoList(medicineRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineSliceResponseDTO getMedicinesSlice(int page, int size) {
        Slice<Medicine> medicinesSlice = medicineRepository.findAllBy(
            PageRequest.of(page, size, Sort.by("name").ascending()));

        return MedicineSliceResponseDTO.builder()
                .content(medicineMapper.toDtoList(medicinesSlice.getContent()))
                .page(medicinesSlice.getNumber())
                .size(medicinesSlice.getSize())
                .hasNext(medicinesSlice.hasNext())
                .hasPrevious(medicinesSlice.hasPrevious())
                .total(medicineRepository.count())
                .inStockCount(medicineRepository.countByQuantityInStockGreaterThan(0))
                .outOfStockCount(medicineRepository.countByQuantityInStockLessThanEqual(0))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> getMedicinesByCategory(String category) {
        return medicineMapper.toDtoList(medicineRepository.findByCategory(MedicineCategory.valueOf(category)));
    }


    @Override
    @Transactional(readOnly = true)
    public boolean existsByMedicineCode(String medicineCode) {
        return medicineRepository.existsByMedicineCode(medicineCode);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }
}
