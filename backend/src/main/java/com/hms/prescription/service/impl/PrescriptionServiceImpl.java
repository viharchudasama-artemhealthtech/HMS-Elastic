package com.hms.prescription.service.impl;

import com.hms.appointment.entity.Appointment;
import com.hms.appointment.exception.AppointmentNotFoundException;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.common.audit.AuditLogService;
import com.hms.common.enums.Role;
import com.hms.doctor.entity.Doctor;
import com.hms.doctor.exception.DoctorNotFoundException;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.patient.entity.Patient;
import com.hms.patient.exception.PatientNotFoundException;
import com.hms.patient.repository.PatientRepository;
import com.hms.pharmacy.entity.InventoryTransaction;
import com.hms.pharmacy.entity.Medicine;
import com.hms.pharmacy.dto.response.MedicineSuggestionDTO;
import com.hms.pharmacy.exception.InsufficientStockException;
import com.hms.pharmacy.exception.MedicineNotFoundException;
import com.hms.pharmacy.repository.InventoryTransactionRepository;
import com.hms.pharmacy.repository.MedicineRepository;
import com.hms.pharmacy.service.search.MedicineSearchService;
import com.hms.prescription.dto.request.PrescriptionMedicineRequestDTO;
import com.hms.prescription.dto.request.PrescriptionRequestDTO;
import com.hms.prescription.dto.response.PrescriptionResponseDTO;
import com.hms.prescription.entity.Prescription;
import com.hms.prescription.entity.PrescriptionMedicine;
import com.hms.prescription.exception.PrescriptionNotFoundException;
import com.hms.prescription.mapper.PrescriptionMapper;
import com.hms.prescription.repository.PrescriptionRepository;
import com.hms.prescription.service.PrescriptionService;
import com.hms.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final MedicineRepository medicineRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final AuditLogService auditLogService;
    private final MedicineSearchService medicineSearchService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found: " + dto.getPatientId()));

        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found: " + dto.getDoctorId()));

        Prescription prescription = prescriptionMapper.toEntity(dto);
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);

        if (dto.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                    .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + dto.getAppointmentId()));
            prescription.setAppointment(appointment);
        }

        if (dto.getMedicines() != null && !dto.getMedicines().isEmpty()) {
            prescription.getMedicines().clear();
            for (PrescriptionMedicineRequestDTO medicineDto : dto.getMedicines()) {
                PrescriptionMedicine medicine = prescriptionMapper.toMedicineEntity(medicineDto);
                prescription.addMedicine(medicine);
            }
        }

        Prescription savedPrescription = prescriptionRepository.save(prescription);

        if (dto.getMedicines() != null && !dto.getMedicines().isEmpty()) {

            for (PrescriptionMedicineRequestDTO medicineDto : dto.getMedicines()) {
                Medicine med = medicineRepository.findByNameIgnoreCase(medicineDto.getMedicineName())
                        .orElseThrow(() -> new MedicineNotFoundException("Medicine not found: " + medicineDto.getMedicineName()));
                
                Integer qty = medicineDto.getQuantity() != null ? medicineDto.getQuantity() : 1;
                
                int updatedRows = medicineRepository.deductStockAtomic(med.getId(), qty);
                if (updatedRows == 0) {
                    throw new InsufficientStockException(med.getName(), "Insufficient stock for medicine: " + med.getName());
                }

                if (med.getQuantityInStock() - qty <= med.getReorderLevel()) {
                    auditLogService.log(null, "LOW_STOCK_AUTO_ALERT", "Medicine", med.getId().toString(), 
                        "Medicine " + med.getName() + " running low.");
                }
                
                InventoryTransaction transaction = InventoryTransaction.builder()
                        .medicine(med)
                        .transactionType("DISPENSE")
                        .quantity(qty)
                        .referenceId(savedPrescription.getId())
                        .notes("Dispensed")
                        .build();
                inventoryTransactionRepository.save(transaction);
            }
        }

        return prescriptionMapper.toDto(savedPrescription);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponseDTO getPrescriptionById(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found: " + id, id.toString()));
        
        checkOwnership(prescription);
        return prescriptionMapper.toDto(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> getAllPrescriptions() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Role role = user.getRole();

        if (role == Role.ADMIN || role == Role.PHARMACIST) {
            return prescriptionMapper.toDtoList(prescriptionRepository.findAll());
        }

        if (role == Role.DOCTOR) {
            return prescriptionMapper.toDtoList(prescriptionRepository.findByDoctorUserId(user.getId()));
        }
        return Collections.emptyList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> getPrescriptionsByPatientId(Long patientId) {
        List<Prescription> prescriptions = prescriptionRepository.findByPatientId(patientId);
        if (!prescriptions.isEmpty()) {
            checkOwnership(prescriptions.get(0));
        }
        return prescriptionMapper.toDtoList(prescriptions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineSuggestionDTO> searchPrescriptionMedicines(String keyword) {
        return medicineSearchService.searchMedicines(keyword);
    }

    @Override
    @Transactional
    public void deletePrescription(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found: " + id, id.toString()));
        
        if (prescription.getMedicines() != null && !prescription.getMedicines().isEmpty()) {
            for (PrescriptionMedicine pm : prescription.getMedicines()) {
                medicineRepository.findByNameIgnoreCase(pm.getMedicineName()).ifPresent(med -> {
                    Integer qty = pm.getQuantity() != null ? pm.getQuantity() : 1;
                    medicineRepository.addStockAtomic(med.getId(), qty);
                    
                    InventoryTransaction transaction = InventoryTransaction.builder()
                            .medicine(med)
                            .transactionType("IN")
                            .quantity(qty)
                            .referenceId(prescription.getId())
                            .notes("Cancelled")
                            .build();
                    inventoryTransactionRepository.save(transaction);
                });
            }
        }

        prescriptionRepository.delete(prescription);
    }

    private void checkOwnership(Prescription prescription) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Role role = user.getRole();

        // 1. Staff with Access
        if (role == Role.ADMIN || role == Role.PHARMACIST) {
            return;
        }

        // 2. Doctor access (Assigned to the patient or wrote it)
        if (role == Role.DOCTOR) {
            if (prescription.getDoctor().getUserId().equals(user.getId())) {
                return;
            }
        }

        log.warn("Security Alert: User {} with role {} tried to access prescription {}.", 
                user.getUsername(), role, prescription.getId());
        throw new AccessDeniedException("You do not have permission to access this prescription.");
    }
}
