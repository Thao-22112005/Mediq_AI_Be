package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.request.FindReplacementCandidatesRequest;
import com.mediq.scheduling.dto.response.ReplacementCandidateDto;

import java.util.List;

public interface ReplacementDoctorService {

    List<ReplacementCandidateDto> findReplacementCandidates(FindReplacementCandidatesRequest request);
}
