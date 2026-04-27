package com.hms.doctor.mapper;

import com.hms.common.enums.Department;
import com.hms.doctor.dto.request.CreateDoctorRequest;
import com.hms.doctor.dto.request.UpdateDoctorRequest;
import com.hms.doctor.dto.response.DoctorResponseDTO;
import com.hms.doctor.entity.Doctor;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-27T10:59:20+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class DoctorMapperImpl implements DoctorMapper {

    @Override
    public Doctor toEntity(CreateDoctorRequest dto) {
        if ( dto == null ) {
            return null;
        }

        Doctor doctor = new Doctor();

        doctor.setUserId( dto.getUserId() );
        doctor.setFirstName( dto.getFirstName() );
        doctor.setLastName( dto.getLastName() );
        doctor.setSpecialization( dto.getSpecialization() );
        doctor.setEmail( dto.getEmail() );
        doctor.setBio( dto.getBio() );
        if ( dto.getDepartment() != null ) {
            doctor.setDepartment( Enum.valueOf( Department.class, dto.getDepartment() ) );
        }
        doctor.setQualification( dto.getQualification() );
        doctor.setExperienceYears( dto.getExperienceYears() );
        doctor.setLicenseNumber( dto.getLicenseNumber() );
        doctor.setConsultationFee( dto.getConsultationFee() );
        doctor.setIsAvailable( dto.getIsAvailable() );
        doctor.setPhoneNumber( dto.getPhoneNumber() );
        doctor.setDesignation( dto.getDesignation() );

        return doctor;
    }

    @Override
    public DoctorResponseDTO toDto(Doctor entity) {
        if ( entity == null ) {
            return null;
        }

        DoctorResponseDTO doctorResponseDTO = new DoctorResponseDTO();

        doctorResponseDTO.setId( entity.getId() );
        doctorResponseDTO.setFirstName( entity.getFirstName() );
        doctorResponseDTO.setLastName( entity.getLastName() );
        doctorResponseDTO.setSpecialization( entity.getSpecialization() );
        doctorResponseDTO.setRegistrationNumber( entity.getRegistrationNumber() );
        if ( entity.getDepartment() != null ) {
            doctorResponseDTO.setDepartment( entity.getDepartment().name() );
        }
        doctorResponseDTO.setEmail( entity.getEmail() );
        doctorResponseDTO.setBio( entity.getBio() );
        doctorResponseDTO.setQualification( entity.getQualification() );
        doctorResponseDTO.setExperienceYears( entity.getExperienceYears() );
        doctorResponseDTO.setLicenseNumber( entity.getLicenseNumber() );
        doctorResponseDTO.setConsultationFee( entity.getConsultationFee() );
        doctorResponseDTO.setIsAvailable( entity.getIsAvailable() );
        doctorResponseDTO.setPhoneNumber( entity.getPhoneNumber() );
        doctorResponseDTO.setDesignation( entity.getDesignation() );

        return doctorResponseDTO;
    }

    @Override
    public void updateEntity(UpdateDoctorRequest dto, Doctor entity) {
        if ( dto == null ) {
            return;
        }

        entity.setFirstName( dto.getFirstName() );
        entity.setLastName( dto.getLastName() );
        entity.setSpecialization( dto.getSpecialization() );
        entity.setBio( dto.getBio() );
        if ( dto.getDepartment() != null ) {
            entity.setDepartment( Enum.valueOf( Department.class, dto.getDepartment() ) );
        }
        else {
            entity.setDepartment( null );
        }
        entity.setQualification( dto.getQualification() );
        entity.setExperienceYears( dto.getExperienceYears() );
        entity.setLicenseNumber( dto.getLicenseNumber() );
        entity.setConsultationFee( dto.getConsultationFee() );
        entity.setIsAvailable( dto.getIsAvailable() );
        entity.setPhoneNumber( dto.getPhoneNumber() );
        entity.setDesignation( dto.getDesignation() );
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
