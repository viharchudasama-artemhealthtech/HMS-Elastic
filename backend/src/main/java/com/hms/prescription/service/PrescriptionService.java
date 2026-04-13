package com.hms.prescription.service;

import com.hms.pharmacy.dto.response.MedicineSuggestionDTO;
import com.hms.prescription.dto.request.PrescriptionRequestDTO;
import com.hms.prescription.dto.response.PrescriptionResponseDTO;

import java.util.List;


public interface PrescriptionService {
    PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO dto);

    PrescriptionResponseDTO getPrescriptionById(Long id);

    List<PrescriptionResponseDTO> getAllPrescriptions();

    List<PrescriptionResponseDTO> getPrescriptionsByPatientId(Long patientId);

    List<MedicineSuggestionDTO> searchPrescriptionMedicines(String keyword);

    void deletePrescription(Long id);
}
