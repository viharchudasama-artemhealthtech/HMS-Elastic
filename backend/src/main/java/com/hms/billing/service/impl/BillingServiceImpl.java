package com.hms.billing.service.impl;

import java.util.UUID;
import com.hms.appointment.entity.Appointment;
import com.hms.appointment.exception.AppointmentNotFoundException;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.billing.dto.request.BillingRequestDTO;
import com.hms.billing.dto.request.BillingItemRequestDTO;
import com.hms.billing.dto.response.BillingResponseDTO;
import com.hms.billing.entity.Billing;
import com.hms.billing.entity.BillingItem;
import com.hms.billing.exception.BillingNotFoundException;
import com.hms.billing.mapper.BillingMapper;
import com.hms.billing.repository.BillingRepository;
import com.hms.billing.service.BillingService;
import com.hms.common.audit.AuditLogService;
import com.hms.common.enums.PaymentMethod;
import com.hms.common.enums.PaymentStatus;
import com.hms.common.enums.Role;
import com.hms.common.exception.BadRequestException;
import com.hms.doctor.entity.Doctor;
import com.hms.patient.entity.Patient;
import com.hms.patient.exception.PatientNotFoundException;
import com.hms.pharmacy.entity.Medicine;
import com.hms.pharmacy.repository.MedicineRepository;
import com.hms.patient.repository.PatientRepository;
import com.hms.prescription.repository.PrescriptionRepository;
import com.hms.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingServiceImpl implements BillingService {

    private final BillingRepository billingRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final BillingMapper billingMapper;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicineRepository medicineRepository;
    private final AuditLogService auditLogService;

    @Value("${hospital.billing.tax-rate:0.05}")
    private BigDecimal taxRate;

    @Override
    @Transactional
    public BillingResponseDTO createBilling(BillingRequestDTO dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found: " + dto.getPatientId()));

        Billing billing = new Billing();
        billing.setPatient(patient);
        billing.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        billing.setBillingDate(dto.getBillingDate());
        billing.setDueDate(dto.getDueDate());
        billing.setTotalAmount(dto.getTotalAmount());
        billing.setTaxAmount(dto.getTaxAmount() != null ? dto.getTaxAmount() : BigDecimal.ZERO);
        billing.setDiscountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : BigDecimal.ZERO);
        billing.setNetAmount(dto.getNetAmount());
        billing.setPaymentStatus(dto.getPaymentStatus());
        billing.setPaymentMethod(dto.getPaymentMethod());
        billing.setNotes(dto.getNotes());

        billing.setInsuranceProvider(dto.getInsuranceProvider());
        billing.setInsuranceClaimNumber(dto.getInsuranceClaimNumber());
        billing.setInsuranceAmount(dto.getInsuranceAmount() != null ? dto.getInsuranceAmount() : BigDecimal.ZERO);
        billing.setInsuranceStatus(dto.getInsuranceStatus() != null ? dto.getInsuranceStatus() : "NONE");

        if (dto.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                    .orElseThrow(
                            () -> new AppointmentNotFoundException("Appointment not found: " + dto.getAppointmentId()));
            billing.setAppointment(appointment);
        }

        if (dto.getItems() != null) {
            for (BillingItemRequestDTO itemDto : dto.getItems()) {
                BillingItem item = new BillingItem();
                item.setItemName(itemDto.getItemName());
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setTotalValue(itemDto.getTotalValue());
                billing.addItem(item);
            }
        }

        if (billing.getTaxAmount().compareTo(BigDecimal.ZERO) == 0
                && billing.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            billing.setTaxAmount(billing.getTotalAmount().multiply(taxRate != null ? taxRate : new BigDecimal("0.05")));
            billing.setNetAmount(
                    billing.getTotalAmount().add(billing.getTaxAmount()).subtract(billing.getDiscountAmount()));
        }

        Billing savedBilling = billingRepository.save(billing);
        auditLogService.log(getCurrentUsername(), "BILLING_CREATE", "Billing", savedBilling.getId().toString(),
                "invoice=" + savedBilling.getInvoiceNumber());
        return billingMapper.toDto(savedBilling);
    }

    @Override
    @Transactional(readOnly = true)
    public BillingResponseDTO getBillingById(Long id) {
        Billing billing = billingRepository.findById(id)
                .orElseThrow(() -> new BillingNotFoundException("Billing not found: " + id, id.toString()));
        checkOwnership(billing);
        return billingMapper.toDto(billing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingResponseDTO> getAllBillings() {
        return billingMapper.toDtoList(billingRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingResponseDTO> getBillingsByPatientId(Long patientId) {
        List<Billing> billings = billingRepository.findByPatientId(patientId);

        if (!billings.isEmpty()) {
            checkOwnership(billings.get(0));
        }

        return billingMapper.toDtoList(billings);
    }

    @Override
    @Transactional
    public BillingResponseDTO updatePaymentStatus(Long id, PaymentStatus status) {
        Billing billing = billingRepository.findById(id)
                .orElseThrow(() -> new BillingNotFoundException("Billing not found: " + id, id.toString()));
        billing.setPaymentStatus(status);
        Billing saved = billingRepository.save(billing);
        auditLogService.log(getCurrentUsername(), "BILLING_PAYMENT_UPDATE", "Billing", id.toString(),
                "status=" + status);
        return billingMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteBilling(Long id) {
        Billing billing = billingRepository.findById(id)
                .orElseThrow(() -> new BillingNotFoundException("Billing not found: " + id, id.toString()));
        billingRepository.delete(billing);
        auditLogService.log(getCurrentUsername(), "BILLING_DELETE", "Billing", id.toString(), "deleted=true");
    }

    @Override
    @Transactional
    public BillingResponseDTO generateBillingFromAppointment(Long appointmentId) {
        Billing billing = prepareBillingFromAppointment(appointmentId);
        Billing saved = billingRepository.save(billing);
        return billingMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BillingResponseDTO calculatePreviewBilling(Long appointmentId) {
        Billing billing = prepareBillingFromAppointment(appointmentId);
        return billingMapper.toDto(billing);
    }

    private Billing prepareBillingFromAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + appointmentId));

        Patient patient = appointment.getPatient();
        Doctor doctor = appointment.getDoctor();

        if (patient == null) {
            throw new BadRequestException("No patient associated with appointment: " + appointmentId);
        }

        Billing billing = new Billing();
        billing.setPatient(patient);
        billing.setAppointment(appointment);
        billing.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        billing.setBillingDate(LocalDateTime.now());
        billing.setPaymentMethod(PaymentMethod.CASH);
        billing.setPaymentStatus(PaymentStatus.UNPAID);

        BigDecimal rate = (this.taxRate != null) ?
                this.taxRate : new BigDecimal("0.05");

        // 1. Registration Fee (entered during patient registration)
        if (patient.getFees() != null && patient.getFees().compareTo(BigDecimal.ZERO) > 0) {
            BillingItem regItem = new BillingItem();
            regItem.setItemName("Registration Fee");
            regItem.setQuantity(1);
            regItem.setUnitPrice(patient.getFees());
            regItem.setTotalValue(patient.getFees());
            billing.addItem(regItem);
        }

        // 2. Doctor Consultation Fee (from doctor profile)
        if (doctor != null && doctor.getConsultationFee() != null && doctor.getConsultationFee().compareTo(BigDecimal.ZERO) > 0) {
            BillingItem consultItem = new BillingItem();
            String doctorLabel = "Consultation - Dr. " + doctor.getLastName();
            consultItem.setItemName(doctorLabel);
            consultItem.setQuantity(1);
            consultItem.setUnitPrice(doctor.getConsultationFee());
            consultItem.setTotalValue(doctor.getConsultationFee());
            billing.addItem(consultItem);
        }

        // 3. Medicine unit price * total quantity fee
        prescriptionRepository.findByAppointmentId(appointmentId).ifPresent(p -> {
            if (p.getMedicines() != null) {
                p.getMedicines().forEach(pm -> {
                    BigDecimal price = medicineRepository.findByNameIgnoreCase(pm.getMedicineName())
                            .map(Medicine::getUnitPrice)
                            .orElse(BigDecimal.ZERO);

                    int qty = (pm.getQuantity() != null) ? pm.getQuantity() : 1;
                    BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));

                    BillingItem item = new BillingItem();
                    item.setItemName("Medicine: " + pm.getMedicineName());
                    item.setQuantity(qty);
                    item.setUnitPrice(price);
                    item.setTotalValue(lineTotal);
                    billing.addItem(item);
                });
            }
        });

        // Set total amount
        BigDecimal subtotal = billing.getItems().stream()
                .map(i -> i.getTotalValue() != null ? i.getTotalValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        billing.setTotalAmount(subtotal);
        billing.setTaxAmount(subtotal.multiply(rate));
        billing.setDiscountAmount(BigDecimal.ZERO);
        billing.setNetAmount(subtotal.add(billing.getTaxAmount()));

        return billing;
    }

    private void checkOwnership(Billing billing) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Role role = user.getRole();

        // 1. Staff with Global Access
        if (role == Role.ADMIN || role == Role.RECEPTIONIST) {
            return;
        }

        log.warn("Security Alert: User {} with role {} attempted unauthorized access to invoice {}.",
                user.getUsername(), role, billing.getInvoiceNumber());
        throw new AccessDeniedException("You do not have permission to access this billing record.");
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }
}
