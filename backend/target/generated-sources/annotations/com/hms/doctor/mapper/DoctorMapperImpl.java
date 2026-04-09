package com.hms.doctor.mapper;

import com.hms.common.enums.Department;
import com.hms.doctor.dto.request.CreateDoctorRequest;
import com.hms.doctor.dto.request.UpdateDoctorRequest;
import com.hms.doctor.dto.response.DoctorResponseDTO;
import com.hms.doctor.entity.Doctor;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/*
@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-09T12:04:40+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
*/
@Component
public class DoctorMapperImpl implements DoctorMapper {

    @Override
    public Doctor toEntity(CreateDoctorRequest dto) {
        if ( dto == null ) {
            return null;
        }

        Doctor doctor = new Doctor();

        doctor.setBio( dto.getBio() );
        doctor.setConsultationFee( dto.getConsultationFee() );
        if ( dto.getDepartment() != null ) {
            doctor.setDepartment( Enum.valueOf( Department.class, dto.getDepartment() ) );
        }
        doctor.setDesignation( dto.getDesignation() );
        doctor.setEmail( dto.getEmail() );
        doctor.setExperienceYears( dto.getExperienceYears() );
        doctor.setFirstName( dto.getFirstName() );
        doctor.setIsAvailable( dto.getIsAvailable() );
        doctor.setLastName( dto.getLastName() );
        doctor.setLicenseNumber( dto.getLicenseNumber() );
        doctor.setPhoneNumber( dto.getPhoneNumber() );
        doctor.setQualification( dto.getQualification() );
        doctor.setSpecialization( dto.getSpecialization() );
        doctor.setUserId( dto.getUserId() );

        return doctor;
    }

    @Override
    public DoctorResponseDTO toDto(Doctor entity) {
        if ( entity == null ) {
            return null;
        }

        DoctorResponseDTO doctorResponseDTO = new DoctorResponseDTO();

        doctorResponseDTO.setBio( entity.getBio() );
        doctorResponseDTO.setConsultationFee( entity.getConsultationFee() );
        if ( entity.getDepartment() != null ) {
            doctorResponseDTO.setDepartment( entity.getDepartment().name() );
        }
        doctorResponseDTO.setDesignation( entity.getDesignation() );
        doctorResponseDTO.setEmail( entity.getEmail() );
        doctorResponseDTO.setExperienceYears( entity.getExperienceYears() );
        doctorResponseDTO.setFirstName( entity.getFirstName() );
        doctorResponseDTO.setId( entity.getId() );
        doctorResponseDTO.setIsAvailable( entity.getIsAvailable() );
        doctorResponseDTO.setLastName( entity.getLastName() );
        doctorResponseDTO.setLicenseNumber( entity.getLicenseNumber() );
        doctorResponseDTO.setPhoneNumber( entity.getPhoneNumber() );
        doctorResponseDTO.setQualification( entity.getQualification() );
        doctorResponseDTO.setRegistrationNumber( entity.getRegistrationNumber() );
        doctorResponseDTO.setSpecialization( entity.getSpecialization() );

        return doctorResponseDTO;
    }

    @Override
    public void updateEntity(UpdateDoctorRequest dto, Doctor entity) {
        if ( dto == null ) {
            return;
        }

        entity.setBio( dto.getBio() );
        entity.setConsultationFee( dto.getConsultationFee() );
        if ( dto.getDepartment() != null ) {
            entity.setDepartment( Enum.valueOf( Department.class, dto.getDepartment() ) );
        }
        else {
            entity.setDepartment( null );
        }
        entity.setDesignation( dto.getDesignation() );
        entity.setExperienceYears( dto.getExperienceYears() );
        entity.setFirstName( dto.getFirstName() );
        entity.setIsAvailable( dto.getIsAvailable() );
        entity.setLastName( dto.getLastName() );
        entity.setLicenseNumber( dto.getLicenseNumber() );
        entity.setPhoneNumber( dto.getPhoneNumber() );
        entity.setQualification( dto.getQualification() );
        entity.setSpecialization( dto.getSpecialization() );
    }

    @Override
    public List<DoctorResponseDTO> toDtoList(List<Doctor> entities) {
        if ( entities == null ) {
            return null;
        }

        List<DoctorResponseDTO> list = new ArrayList<DoctorResponseDTO>( entities.size() );
        for ( Doctor doctor : entities ) {
            list.add( toDto( doctor ) );
        }

        return list;
    }
}
