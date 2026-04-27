package com.hms.pharmacy.controller;

import com.hms.common.response.ApiResponse;
import com.hms.pharmacy.dto.request.DispenseMedicineRequestDTO;
import com.hms.pharmacy.dto.request.MedicineRequestDTO;
import com.hms.pharmacy.dto.request.RestockMedicineRequestDTO;
import com.hms.pharmacy.dto.response.MedicineResponseDTO;
import com.hms.pharmacy.dto.response.MedicineSliceResponseDTO;
import com.hms.pharmacy.dto.response.MedicineSuggestionDTO;
import com.hms.pharmacy.service.InventoryService;
import com.hms.pharmacy.service.MedicineService;
import com.hms.pharmacy.service.search.MedicineSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MedicineController {

    private final MedicineService service;
    private final InventoryService inventoryService;
    private final MedicineSearchService medicineSearchService;

    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    @PostMapping
    public ResponseEntity<ApiResponse<MedicineResponseDTO>> create(
            @Valid @RequestBody MedicineRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse
                        .success(service.createMedicine(dto),
                                "Request successful",
                                HttpStatus.CREATED));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponseDTO>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody MedicineRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(service.updateMedicine(id, dto)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        service.deleteMedicine(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PHARMACIST')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponseDTO>> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getMedicineById(id)));
    }

    @PreAuthorize("hasAnyRole('PHARMACIST', 'DOCTOR', 'ADMIN', 'NURSE')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllMedicines()));
    }

    @PreAuthorize("hasAnyRole('PHARMACIST', 'DOCTOR', 'ADMIN', 'NURSE')")
    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<MedicineSliceResponseDTO>> getPaged(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.getMedicinesSlice(page, size)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PHARMACIST')")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> getActive() {
        return ResponseEntity.ok(ApiResponse.success(medicineSearchService.getActiveMedicines()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PHARMACIST','NURSE')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MedicineSuggestionDTO>>> search(
            @RequestParam("q") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                medicineSearchService.searchMedicines(keyword, page, size),
                "Medicine search completed"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PHARMACIST')")
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> getByCategory(
            @PathVariable("category") String category) {
        return ResponseEntity.ok(ApiResponse.success(service.getMedicinesByCategory(category)));
    }

    @GetMapping("/check-code/{code}")
    public ResponseEntity<ApiResponse<Boolean>> checkCode(@PathVariable("code") String code) {
        return ResponseEntity.ok(ApiResponse.success(service.existsByMedicineCode(code)));
    }

    @PreAuthorize("hasRole('PHARMACIST')")
    @PostMapping("/dispense")
    public ResponseEntity<ApiResponse<Void>> dispense(
            @Valid @RequestBody DispenseMedicineRequestDTO request) {
        inventoryService.dispenseMedicines(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    @PatchMapping("/{id}/restock")
    public ResponseEntity<ApiResponse<Void>> restock(
            @PathVariable("id") Long id,
            @Valid @RequestBody RestockMedicineRequestDTO request) {
        inventoryService.restockMedicine(id, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reindex")
    public ResponseEntity<ApiResponse<String>> reindexMedicines() {
        try {
            medicineSearchService.reindexAllMedicines();
            return ResponseEntity.ok(ApiResponse.success("All medicines reindex to Elasticsearch successfully"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>builder()
                            .success(false)
                            .message("Reindex failed: " + ex.getMessage())
                            .status(400)
                            .build());
        }
    }
}
