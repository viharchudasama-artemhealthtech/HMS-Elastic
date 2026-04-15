package com.hms.prescription.mapper;

import com.hms.appointment.entity.Appointment;
import com.hms.doctor.entity.Doctor;
import com.hms.patient.entity.Patient;
import com.hms.prescription.dto.request.PrescriptionMedicineRequestDTO;
import com.hms.prescription.dto.request.PrescriptionRequestDTO;
import com.hms.prescription.dto.response.PrescriptionMedicineResponseDTO;
import com.hms.prescription.dto.response.PrescriptionResponseDTO;
import com.hms.prescription.entity.Prescription;
import com.hms.prescription.entity.PrescriptionMedicine;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/*
@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-14T18:46:07+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
*/
@Component
public class PrescriptionMapperImpl implements PrescriptionMapper {

    @Override
    public Prescription toEntity(PrescriptionRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Prescription prescription = new Prescription();

        prescription.setAdvice( dto.getAdvice() );
        prescription.setDiagnosis( dto.getDiagnosis() );
        prescription.setMedicines( prescriptionMedicineRequestDTOListToPrescriptionMedicineList( dto.getMedicines() ) );
        prescription.setNotes( dto.getNotes() );
        prescription.setSymptoms( dto.getSymptoms() );

        return prescription;
    }

    @Override
    public PrescriptionResponseDTO toDto(Prescription entity) {
        if ( entity == null ) {
            return null;
        }

        PrescriptionResponseDTO prescriptionResponseDTO = new PrescriptionResponseDTO();

        prescriptionResponseDTO.setPatientId( entityPatientId( entity ) );
        prescriptionResponseDTO.setPatientName( entityPatientName( entity ) );
        prescriptionResponseDTO.setDoctorId( entityDoctorId( entity ) );
        prescriptionResponseDTO.setAppointmentId( entityAppointmentId( entity ) );
        prescriptionResponseDTO.setAdvice( entity.getAdvice() );
        prescriptionResponseDTO.setCreatedAt( entity.getCreatedAt() );
        prescriptionResponseDTO.setDiagnosis( entity.getDiagnosis() );
        prescriptionResponseDTO.setId( entity.getId() );
        prescriptionResponseDTO.setMedicines( prescriptionMedicineListToPrescriptionMedicineResponseDTOList( entity.getMedicines() ) );
        prescriptionResponseDTO.setNotes( entity.getNotes() );
        prescriptionResponseDTO.setSymptoms( entity.getSymptoms() );

        prescriptionResponseDTO.setDoctorName( entity.getDoctor() != null ? entity.getDoctor().getFirstName() + " " + entity.getDoctor().getLastName() : null );

        return prescriptionResponseDTO;
    }

    @Override
    public List<PrescriptionResponseDTO> toDtoList(List<Prescription> entities) {
        if ( entities == null ) {
            return null;
        }

        List<PrescriptionResponseDTO> list = new ArrayList<PrescriptionResponseDTO>( entities.size() );
        for ( Prescription prescription : entities ) {
            list.add( toDto( prescription ) );
        }

        return list;
    }

    @Override
    public PrescriptionMedicineResponseDTO toMedicineDto(PrescriptionMedicine entity) {
        if ( entity == null ) {
            return null;
        }

        PrescriptionMedicineResponseDTO prescriptionMedicineResponseDTO = new PrescriptionMedicineResponseDTO();

        prescriptionMedicineResponseDTO.setDosage( entity.getDosage() );
        prescriptionMedicineResponseDTO.setDuration( entity.getDuration() );
        prescriptionMedicineResponseDTO.setId( entity.getId() );
        prescriptionMedicineResponseDTO.setInstructions( entity.getInstructions() );
        prescriptionMedicineResponseDTO.setMedicineName( entity.getMedicineName() );
        prescriptionMedicineResponseDTO.setQuantity( entity.getQuantity() );

        return prescriptionMedicineResponseDTO;
    }

    @Override
    public PrescriptionMedicine toMedicineEntity(PrescriptionMedicineRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        PrescriptionMedicine prescriptionMedicine = new PrescriptionMedicine();

        prescriptionMedicine.setDosage( dto.getDosage() );
        prescriptionMedicine.setDuration( dto.getDuration() );
        prescriptionMedicine.setInstructions( dto.getInstructions() );
        prescriptionMedicine.setMedicineName( dto.getMedicineName() );
        prescriptionMedicine.setQuantity( dto.getQuantity() );

        return prescriptionMedicine;
    }

    protected List<PrescriptionMedicine> prescriptionMedicineRequestDTOListToPrescriptionMedicineList(List<PrescriptionMedicineRequestDTO> list) {
        if ( list == null ) {
            return null;
        }

        List<PrescriptionMedicine> list1 = new ArrayList<PrescriptionMedicine>( list.size() );
        for ( PrescriptionMedicineRequestDTO prescriptionMedicineRequestDTO : list ) {
            list1.add( toMedicineEntity( prescriptionMedicineRequestDTO ) );
        }

        return list1;
    }

    private Long entityPatientId(Prescription prescription) {
        if ( prescription == null ) {
            return null;
        }
        Patient patient = prescription.getPatient();
        if ( patient == null ) {
            return null;
        }
        Long id = patient.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityPatientName(Prescription prescription) {
        if ( prescription == null ) {
            return null;
        }
        Patient patient = prescription.getPatient();
        if ( patient == null ) {
            return null;
        }
        String name = patient.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long entityDoctorId(Prescription prescription) {
        if ( prescription == null ) {
            return null;
        }
        Doctor doctor = prescription.getDoctor();
        if ( doctor == null ) {
            return null;
        }
        Long id = doctor.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long entityAppointmentId(Prescription prescription) {
        if ( prescription == null ) {
            return null;
        }
        Appointment appointment = prescription.getAppointment();
        if ( appointment == null ) {
            return null;
        }
        Long id = appointment.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected List<PrescriptionMedicineResponseDTO> prescriptionMedicineListToPrescriptionMedicineResponseDTOList(List<PrescriptionMedicine> list) {
        if ( list == null ) {
            return null;
        }

        List<PrescriptionMedicineResponseDTO> list1 = new ArrayList<PrescriptionMedicineResponseDTO>( list.size() );
        for ( PrescriptionMedicine prescriptionMedicine : list ) {
            list1.add( toMedicineDto( prescriptionMedicine ) );
        }

        return list1;
    }
}
