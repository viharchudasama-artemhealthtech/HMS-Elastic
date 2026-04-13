package com.hms.common.exception;

import com.hms.appointment.exception.AppointmentNotFoundException;
import com.hms.appointment.exception.DoctorUnavailableException;
import com.hms.appointment.exception.SlotAlreadyBookedException;
import com.hms.billing.exception.BillingNotFoundException;
import com.hms.doctor.exception.DoctorNotFoundException;
import com.hms.prescription.exception.PrescriptionNotFoundException;
import com.hms.user.exception.UserNotFoundException;

import com.hms.common.response.ApiError;
import com.hms.common.response.ValidationError;
import com.hms.patient.exception.DuplicatePatientException;
import com.hms.patient.exception.PatientNotFoundException;
import com.hms.pharmacy.exception.DuplicateMedicineException;
import com.hms.pharmacy.exception.InsufficientStockException;
import com.hms.pharmacy.exception.MedicineNotFoundException;
import com.hms.user.exception.EmailAlreadyExistsException;
import com.hms.user.exception.InvalidCredentialsException;
import com.hms.user.exception.UsernameAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// Handle Exception Globally and return Json format respons
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        // ========== Standard Response Build Helper ==========
        private ResponseEntity<ApiError> buildResponse(HmsErrorCode errorCode, HttpStatus status, String customMessage, HttpServletRequest request) {
            ApiError response = ApiError.of(
                    customMessage != null ? customMessage : errorCode.getDefaultMessage(),
                    errorCode.getCode(),
                    status
            );
            response.setPath(request.getRequestURI());
            return ResponseEntity.status(status).body(response);
        }

        // ========== Patient Exceptions ==========

        @ExceptionHandler(PatientNotFoundException.class)
        public ResponseEntity<ApiError> handlePatientNotFound(PatientNotFoundException ex, HttpServletRequest request) {
                log.warn("Patient not found: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.PATIENT_NOT_FOUND, HttpStatus.NOT_FOUND, ex.getMessage(), request);
        }

        @ExceptionHandler(DuplicatePatientException.class)
        public ResponseEntity<ApiError> handleDuplicatePatient(DuplicatePatientException ex, HttpServletRequest request) {
                log.warn("Duplicate patient: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.DUPLICATE_PATIENT, HttpStatus.CONFLICT, ex.getMessage(), request);
        }

        // ========== Appointment Exceptions ==========

        @ExceptionHandler(SlotAlreadyBookedException.class)
        public ResponseEntity<ApiError> handleSlotAlreadyBooked(SlotAlreadyBookedException ex, HttpServletRequest request) {
                log.warn("Slot already booked: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.SLOT_OCCUPIED, HttpStatus.CONFLICT, ex.getMessage(), request);
        }

        @ExceptionHandler(AppointmentNotFoundException.class)
        public ResponseEntity<ApiError> handleAppointmentNotFound(AppointmentNotFoundException ex, HttpServletRequest request) {
                log.warn("Appointment not found: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.APPOINTMENT_NOT_FOUND, HttpStatus.NOT_FOUND, ex.getMessage(), request);
        }

        @ExceptionHandler(DoctorUnavailableException.class)
        public ResponseEntity<ApiError> handleDoctorUnavailable(DoctorUnavailableException ex, HttpServletRequest request) {
                log.warn("Doctor unavailable: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.DOCTOR_UNAVAILABLE, HttpStatus.CONFLICT, ex.getMessage(), request);
        }

        // ========== User/Auth Exceptions ==========

        @ExceptionHandler(InvalidCredentialsException.class)
        public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
                log.warn("Invalid credentials: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED, null, request);
        }

        @ExceptionHandler(UsernameAlreadyExistsException.class)
        public ResponseEntity<ApiError> handleUsernameExists(UsernameAlreadyExistsException ex, HttpServletRequest request) {
                log.warn("Username already exists: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.USERNAME_EXISTS, HttpStatus.CONFLICT, ex.getMessage(), request);
        }

        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<ApiError> handleEmailExists(EmailAlreadyExistsException ex, HttpServletRequest request) {
                log.warn("Email already exists: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.CONFLICT, ex.getMessage(), request);
        }

        @ExceptionHandler(InsufficientStockException.class)
        public ResponseEntity<ApiError> handleInsufficientStock(InsufficientStockException ex, HttpServletRequest request) {
                log.warn("Pharmacy stock low for {}: {}", ex.getMedicineName(), ex.getMessage());
                return buildResponse(HmsErrorCode.INSUFFICIENT_STOCK, HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        }

        @ExceptionHandler(MedicineNotFoundException.class)
        public ResponseEntity<ApiError> handleMedicineNotFound(MedicineNotFoundException ex, HttpServletRequest request) {
                log.warn("Medicine not found: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.MEDICINE_NOT_FOUND, HttpStatus.NOT_FOUND, ex.getMessage(), request);
        }

        @ExceptionHandler(DuplicateMedicineException.class)
        public ResponseEntity<ApiError> handleDuplicateMedicine(DuplicateMedicineException ex, HttpServletRequest request) {
                log.warn("Duplicate medicine: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.DUPLICATE_MEDICINE, HttpStatus.CONFLICT, ex.getMessage(), request);
        }

        // ========== Doctor Exceptions ==========

        @ExceptionHandler(DoctorNotFoundException.class)
        public ResponseEntity<ApiError> handleDoctorNotFound(DoctorNotFoundException ex, HttpServletRequest request) {
                log.warn("Doctor not found: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.DOCTOR_NOT_FOUND, HttpStatus.NOT_FOUND, ex.getMessage(), request);
        }

        // ========== Billing Exceptions ==========

        @ExceptionHandler(BillingNotFoundException.class)
        public ResponseEntity<ApiError> handleBillingNotFound(BillingNotFoundException ex, HttpServletRequest request) {
                log.warn("Billing record not found: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.BILLING_NOT_FOUND, HttpStatus.NOT_FOUND, ex.getMessage(), request);
        }


        // ========== Prescription Exceptions ==========

        @ExceptionHandler(PrescriptionNotFoundException.class)
        public ResponseEntity<ApiError> handlePrescriptionNotFound(PrescriptionNotFoundException ex, HttpServletRequest request) {
                log.warn("Prescription not found: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.PRESCRIPTION_NOT_FOUND, HttpStatus.NOT_FOUND, ex.getMessage(), request);
        }

        // ========== User Exceptions ==========

        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
                log.warn("User not found: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND, ex.getMessage(), request);
        }

        // ========== Validation & Security ==========

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
                log.warn("Bad request: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
                List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                        .map(e -> new ValidationError(e.getField(), e.getDefaultMessage()))
                        .toList();

                log.warn("Validation failed for {}: {}", request.getRequestURI(), errors);
                ApiError response = ApiError.of("Validation failed", HmsErrorCode.VALIDATION_FAILED.getCode(), HttpStatus.BAD_REQUEST);
                response.setValidationErrors(errors);
                response.setPath(request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
                log.warn("Access denied: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN, null, request);
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiError> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
                log.warn("Authentication failed: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleGeneral(Exception ex, HttpServletRequest request) {
                log.error("Unexpected error occurred at {}", request.getRequestURI(), ex);
                return buildResponse(HmsErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null, request);
        }
}
