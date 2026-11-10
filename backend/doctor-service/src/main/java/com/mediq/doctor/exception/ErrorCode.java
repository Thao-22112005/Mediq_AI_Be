package com.mediq.doctor.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    DOCTOR_NOT_FOUND(HttpStatus.NOT_FOUND, "Doctor not found"),
    DOCTOR_INACTIVE(HttpStatus.BAD_REQUEST, "Doctor is currently inactive or suspended"),
    LICENSE_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "Doctor license number already exists"),
    USER_ID_ALREADY_EXISTS(HttpStatus.CONFLICT, "User ID is already linked to another doctor"),
    SPECIALTY_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "Specialty is already assigned to this doctor"),
    SPECIALTY_NOT_FOUND(HttpStatus.NOT_FOUND, "Specialty not found for this doctor"),
    PRIMARY_SPECIALTY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Doctor can have at most one primary specialty"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request data");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
