package com.hms.patient.mapper;

import com.hms.patient.dto.request.PatientRequestDTO;
import com.hms.patient.dto.response.PatientResponseDTO;
import com.hms.patient.entity.Patient;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-27T17:48:54+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Microsoft)"
)
@Component
public class PatientMapperImpl implements PatientMapper {

    @Override
    public Patient toEntity(PatientRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Patient patient = new Patient();

        patient.setName( dto.getName() );
        patient.setAge( dto.getAge() );
        patient.setBloodGroup( dto.getBloodGroup() );
        patient.setUrgencyLevel( dto.getUrgencyLevel() );
        patient.setPrescription( dto.getPrescription() );
        patient.setDose( dto.getDose() );
        patient.setFees( dto.getFees() );
        patient.setContactNumber( dto.getContactNumber() );
        patient.setEmail( dto.getEmail() );

        return patient;
    }

    @Override
    public PatientResponseDTO toResponse(Patient patient) {
        if ( patient == null ) {
            return null;
        }

        PatientResponseDTO patientResponseDTO = new PatientResponseDTO();

        patientResponseDTO.setId( patient.getId() );
        patientResponseDTO.setName( patient.getName() );
        if ( patient.getAge() != null ) {
            patientResponseDTO.setAge( patient.getAge() );
        }
        patientResponseDTO.setBloodGroup( patient.getBloodGroup() );
        patientResponseDTO.setPrescription( patient.getPrescription() );
        patientResponseDTO.setDose( patient.getDose() );
        patientResponseDTO.setFees( patient.getFees() );
        patientResponseDTO.setContactNumber( patient.getContactNumber() );
        patientResponseDTO.setUrgencyLevel( patient.getUrgencyLevel() );
        patientResponseDTO.setCreatedAt( patient.getCreatedAt() );

        return patientResponseDTO;
    }

    @Override
    public void updateEntity(PatientRequestDTO dto, Patient patient) {
        if ( dto == null ) {
            return;
        }

        patient.setName( dto.getName() );
        patient.setAge( dto.getAge() );
        patient.setBloodGroup( dto.getBloodGroup() );
        patient.setUrgencyLevel( dto.getUrgencyLevel() );
        patient.setPrescription( dto.getPrescription() );
        patient.setDose( dto.getDose() );
        patient.setFees( dto.getFees() );
        patient.setContactNumber( dto.getContactNumber() );
        patient.setEmail( dto.getEmail() );
    }
}
