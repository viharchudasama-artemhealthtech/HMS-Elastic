package com.hms.common.exception;

public enum HmsErrorCode {
    
    // General Errors
    INTERNAL_SERVER_ERROR("ERR_INTERNAL", "An unexpected error occurred"),
    VALIDATION_FAILED("ERR_VALIDATION", "Entity validation failed"),
    ACCESS_DENIED("ERR_FORBIDDEN", "Access denied for this resource"),
    UNAUTHORIZED("ERR_UNAUTHORIZED", "Authentication required"),
    
    // Authentication Errors
    INVALID_CREDENTIALS("AUTH_001", "Invalid username or password"),
    USERNAME_EXISTS("AUTH_002", "Username already taken"),
    TOKEN_EXPIRED("AUTH_003", "Session has expired"),
    EMAIL_ALREADY_EXISTS("AUTH_004", "Email address already in use"),
    
    // Patient Errors
    PATIENT_NOT_FOUND("PAT_001", "Patient not found"),
    DUPLICATE_PATIENT("PAT_002", "Patient already exists"),
    
    // Appointment Errors
    APPOINTMENT_NOT_FOUND("APP_001", "Appointment not found"),
    SLOT_OCCUPIED("APP_002", "Selected time slot is already booked"),
    DOCTOR_UNAVAILABLE("APP_003", "Doctor is not available at the requested time"),

    // Doctor Errors
    DOCTOR_NOT_FOUND("DOC_001", "Doctor record not found"),

    // Request Errors
    BAD_REQUEST("REQ_001", "The request is invalid"),
    INVALID_REQUEST_BODY("REQ_002", "Malformed request body"),
    INVALID_PARAMETER("REQ_003", "One or more request parameters are invalid"),
    MISSING_PARAMETER("REQ_004", "Required request parameter is missing"),
    METHOD_NOT_ALLOWED("REQ_005", "HTTP method is not allowed for this endpoint"),
    DATA_INTEGRITY_VIOLATION("REQ_006", "Operation violates data integrity constraints"),
    OPERATION_NOT_ALLOWED("REQ_007", "Operation is not allowed in current state"),
    
    // User Errors
    USER_NOT_FOUND("AUTH_006", "User record not found"),
    
    // Pharmacy Errors
    MEDICINE_NOT_FOUND("PHR_001", "Medicine not found in inventory"),
    INSUFFICIENT_STOCK("PHR_002", "Insufficient medicine quantity in stock"),
    DUPLICATE_MEDICINE("PHR_003", "Medicine code already exists"),


    // Billing Errors
    BILLING_NOT_FOUND("BIL_001", "Billing record not found"),

    // Prescription Errors
    PRESCRIPTION_NOT_FOUND("PRE_001", "Prescription not found");

    private final String code;
    private final String defaultMessage;

    HmsErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
