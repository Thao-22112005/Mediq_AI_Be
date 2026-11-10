package com.mediq.scheduling.service;

import com.mediq.scheduling.client.DoctorClient;
import com.mediq.scheduling.dto.request.FindReplacementCandidatesRequest;
import com.mediq.scheduling.dto.response.ReplacementCandidateDto;
import com.mediq.scheduling.entity.Slot;
import com.mediq.scheduling.entity.SlotStatus;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import com.mediq.scheduling.repository.LeaveRequestRepository;
import com.mediq.scheduling.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplacementDoctorServiceImpl implements ReplacementDoctorService {

    private final SlotRepository slotRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final DoctorClient doctorClient;

    @Override
    public List<ReplacementCandidateDto> findReplacementCandidates(FindReplacementCandidatesRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Request body cannot be null");
        }
        if (request.getSpecialtyId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Specialty ID is required to find replacement doctor");
        }
        if (request.getDate() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Date is required to find replacement doctor");
        }
        if (request.getStartTime() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Start time is required to find replacement doctor");
        }

        // 1. Fetch available slots for the given specialty on the specified date
        List<Slot> availableSlots = slotRepository.findSlotsWithFilter(
                null,
                null,
                request.getSpecialtyId(),
                request.getDate(),
                SlotStatus.AVAILABLE
        );

        List<ReplacementCandidateDto> candidates = new ArrayList<>();

        for (Slot slot : availableSlots) {
            UUID candidateDoctorId = slot.getWorkSchedule().getDoctorId();

            // Criterion 1: Exclude the original doctor going on leave
            if (request.getOriginalDoctorId() != null && request.getOriginalDoctorId().equals(candidateDoctorId)) {
                continue;
            }

            // Criterion 2: Match start time
            if (!slot.getStartTime().equals(request.getStartTime())) {
                continue;
            }
            if (request.getEndTime() != null && !slot.getEndTime().equals(request.getEndTime())) {
                continue;
            }

            // Criterion 3: Doctor must be ACTIVE (WF-04 / BR-LEAVE-006)
            if (!doctorClient.isDoctorActive(candidateDoctorId)) {
                continue;
            }

            // Criterion 4: Doctor must NOT be on leave (WF-04 / BR-LEAVE-006)
            LocalDateTime slotStart = LocalDateTime.of(slot.getWorkSchedule().getDate(), slot.getStartTime());
            LocalDateTime slotEnd = LocalDateTime.of(slot.getWorkSchedule().getDate(), slot.getEndTime());
            boolean onLeave = leaveRequestRepository.hasOverlappingLeave(candidateDoctorId, slotStart, slotEnd);
            if (onLeave) {
                continue;
            }

            // BR-LEAVE-007 / WF-04: Cross-Clinic handling & prioritization
            UUID clinicId = slot.getWorkSchedule().getClinic().getClinicId();
            boolean isCrossClinic = request.getCurrentClinicId() != null && !clinicId.equals(request.getCurrentClinicId());
            int priority = isCrossClinic ? 2 : 1;

            DoctorClient.DoctorDto doctorDto = doctorClient.getDoctor(candidateDoctorId).orElse(null);
            String doctorName = (doctorDto != null && doctorDto.fullName() != null)
                    ? doctorDto.fullName()
                    : "Dr. " + candidateDoctorId;

            candidates.add(new ReplacementCandidateDto(
                    slot.getSlotId(),
                    candidateDoctorId,
                    doctorName,
                    clinicId,
                    slot.getWorkSchedule().getClinic().getName(),
                    slot.getWorkSchedule().getRoom().getRoomId(),
                    slot.getWorkSchedule().getRoom().getName(),
                    slot.getWorkSchedule().getSpecialty().getSpecialtyId(),
                    slot.getWorkSchedule().getSpecialty().getName(),
                    slot.getWorkSchedule().getDate(),
                    slot.getStartTime(),
                    slot.getEndTime(),
                    isCrossClinic,
                    priority
            ));
        }

        // Sort by priority ASC (Priority 1: same clinic, Priority 2: cross-clinic), then clinicName ASC, doctorName ASC
        candidates.sort(Comparator
                .comparingInt(ReplacementCandidateDto::priority)
                .thenComparing(ReplacementCandidateDto::clinicName)
                .thenComparing(ReplacementCandidateDto::doctorName)
        );

        return candidates;
    }
}
