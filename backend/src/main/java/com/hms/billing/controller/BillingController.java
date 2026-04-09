package com.hms.billing.controller;

import com.hms.common.response.ApiResponse;
import com.hms.billing.dto.request.BillingRequestDTO;
import com.hms.billing.dto.response.BillingResponseDTO;
import com.hms.common.enums.PaymentStatus;
import com.hms.billing.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/billings")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class BillingController {

    private final BillingService billingService;

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @PostMapping
    public ResponseEntity<ApiResponse<BillingResponseDTO>> createBilling(
            @Valid @RequestBody BillingRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(billingService.createBilling(dto)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BillingResponseDTO>>> getAllBillings() {
        return ResponseEntity.ok(ApiResponse.success(
                billingService.getAllBillings()));
    }


    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<BillingResponseDTO>>> getBillingsByPatientId(
            @PathVariable("patientId") Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(
                billingService.getBillingsByPatientId(patientId)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @GetMapping("/preview-appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<BillingResponseDTO>> previewBillingJson(
            @PathVariable("appointmentId") Long appointmentId) {
        return ResponseEntity.ok(ApiResponse.success(
                billingService.calculatePreviewBilling(appointmentId)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillingResponseDTO>> getBillingById(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                billingService.getBillingById(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BillingResponseDTO>> updatePaymentStatus(
            @PathVariable("id") Long id, @RequestParam("status") PaymentStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                billingService.updatePaymentStatus(id, status)));
    }



    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @PostMapping("/generate/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<BillingResponseDTO>> generateBilling(
            @PathVariable("appointmentId") Long appointmentId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        billingService.generateBillingFromAppointment(appointmentId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBilling(
            @PathVariable("id") Long id) {
        billingService.deleteBilling(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
