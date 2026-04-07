package com.hms.patient.mapper;

import com.hms.patient.dto.request.PatientRequestDTO;
import com.hms.patient.dto.response.PatientResponseDTO;
import com.hms.patient.entity.Patient;
import org.springframework.stereotype.Component;

/*
@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-07T10:36:35+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
*/
@Component
public class PatientMapperImpl implements PatientMapper {

    @Override
    public Patient toEntity(PatientRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Patient patient = new Patient();

        patient.setAge( dto.getAge() );
        patient.setBloodGroup( dto.getBloodGroup() );
        patient.setContactNumber( dto.getContactNumber() );
        patient.setDose( dto.getDose() );
        patient.setEmail( dto.getEmail() );
        patient.setFees( dto.getFees() );
        patient.setName( dto.getName() );
        patient.setPrescription( dto.getPrescription() );
        patient.setUrgencyLevel( dto.getUrgencyLevel() );

        return patient;
    }

    @Override
    public PatientResponseDTO toResponse(Patient patient) {
        if ( patient == null ) {
            return null;
        }

        PatientResponseDTO patientResponseDTO = new PatientResponseDTO();

        if ( patient.getAge() != null ) {
            patientResponseDTO.setAge( patient.getAge() );
        }
        patientResponseDTO.setBloodGroup( patient.getBloodGroup() );
        patientResponseDTO.setContactNumber( patient.getContactNumber() );
        patientResponseDTO.setCreatedAt( patient.getCreatedAt() );
        patientResponseDTO.setDose( patient.getDose() );
        patientResponseDTO.setFees( patient.getFees() );
        patientResponseDTO.setId( patient.getId() );
        patientResponseDTO.setName( patient.getName() );
        patientResponseDTO.setPrescription( patient.getPrescription() );
        patientResponseDTO.setUrgencyLevel( patient.getUrgencyLevel() );

        return patientResponseDTO;
    }

    @Override
    public void updateEntity(PatientRequestDTO dto, Patient patient) {
        if ( dto == null ) {
            return;
        }

        patient.setAge( dto.getAge() );
        patient.setBloodGroup( dto.getBloodGroup() );
        patient.setContactNumber( dto.getContactNumber() );
        patient.setDose( dto.getDose() );
        patient.setEmail( dto.getEmail() );
        patient.setFees( dto.getFees() );
        patient.setName( dto.getName() );
        patient.setPrescription( dto.getPrescription() );
        patient.setUrgencyLevel( dto.getUrgencyLevel() );
    }
}
