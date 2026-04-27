package com.hms.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private boolean success;
    private String message;
    private String errorCode;
    private int status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private String path;
    private List<ValidationError> validationErrors;

    public ApiError() {
    }

    public ApiError(boolean success, String message, String errorCode, int status, LocalDateTime timestamp, String path, List<ValidationError> validationErrors) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.status = status;
        this.timestamp = timestamp;
        this.path = path;
        this.validationErrors = validationErrors;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<ValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public static ApiError of(String message, String errorCode, HttpStatus status) {
        ApiError error = new ApiError();
        error.setSuccess(false);
        error.setMessage(message);
        error.setErrorCode(errorCode);
        error.setStatus(status.value());
        error.setTimestamp(LocalDateTime.now());
        return error;
    }
}
