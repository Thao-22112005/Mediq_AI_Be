package com.mediq.scheduling.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    CLINIC_NOT_FOUND(HttpStatus.NOT_FOUND, "Clinic not found"),
    CLINIC_CODE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Clinic code already exists"),
    CLINIC_INACTIVE(HttpStatus.BAD_REQUEST, "Clinic is inactive"),
    SPECIALTY_NOT_FOUND(HttpStatus.NOT_FOUND, "Specialty not found"),
    SPECIALTY_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Specialty name already exists"),
    SPECIALTY_INACTIVE(HttpStatus.BAD_REQUEST, "Specialty is inactive"),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "Room not found"),
    ROOM_CODE_ALREADY_EXISTS_IN_CLINIC(HttpStatus.CONFLICT, "Room code already exists in this clinic"),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "Work schedule not found"),
    ROOM_CLINIC_MISMATCH(HttpStatus.BAD_REQUEST, "Room does not belong to the specified clinic"),
    ROOM_SPECIALTY_MISMATCH(HttpStatus.BAD_REQUEST, "Room specialty does not match schedule specialty"),
    DOCTOR_SPECIALTY_MISMATCH(HttpStatus.BAD_REQUEST, "Doctor does not have the required specialty for this schedule"),
    DOCTOR_SCHEDULE_OVERLAP(HttpStatus.CONFLICT, "Doctor already has an overlapping work schedule"),
    ROOM_SCHEDULE_OVERLAP(HttpStatus.CONFLICT, "Room already has an overlapping work schedule"),
    SCHEDULE_ALREADY_APPROVED(HttpStatus.BAD_REQUEST, "Work schedule is already approved"),
    SCHEDULE_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "Work schedule is already cancelled"),
    PAST_SCHEDULE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Cannot create schedule in the past"),
    SLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "Slot not found"),
    SLOT_NOT_AVAILABLE(HttpStatus.CONFLICT, "Slot is not available"),
    SLOT_ALREADY_BOOKED(HttpStatus.CONFLICT, "Slot is already booked"),
    PAST_SLOT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Cannot hold or book a slot in the past"),
    INVALID_SLOT_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "Invalid slot status transition"),
    LEAVE_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "Leave request not found"),
    LEAVE_REQUEST_OVERLAP(HttpStatus.CONFLICT, "Doctor already has an overlapping leave request"),
    PAST_LEAVE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Cannot request leave in the past"),
    INVALID_LEAVE_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "Invalid leave request status transition"),
    DOCTOR_CANNOT_APPROVE_OWN_LEAVE(HttpStatus.FORBIDDEN, "Doctor cannot approve their own leave request"),
    SCHEDULE_CHANGE_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "Schedule change request not found"),
    INVALID_SCHEDULE_CHANGE_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "Invalid schedule change request status transition"),
    SCHEDULE_HAS_BOOKED_APPOINTMENTS(HttpStatus.CONFLICT, "Schedule has booked appointments that must be handled before changing schedule"),
    DOCTOR_CANNOT_APPROVE_OWN_SCHEDULE_CHANGE(HttpStatus.FORBIDDEN, "Doctor cannot approve their own schedule change request"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request data");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
