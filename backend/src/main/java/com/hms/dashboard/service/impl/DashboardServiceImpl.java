package com.hms.dashboard.service.impl;

import com.hms.appointment.repository.AppointmentRepository;
import com.hms.billing.repository.BillingRepository;
import com.hms.common.enums.AppointmentStatus;
import com.hms.common.enums.Department;
import com.hms.dashboard.dto.DashboardSummaryDTO;
import com.hms.dashboard.dto.DepartmentStatisticsDTO;
import com.hms.dashboard.dto.WeeklyStatisticsDTO;
import com.hms.dashboard.service.DashboardService;
import com.hms.patient.repository.PatientRepository;
import com.hms.pharmacy.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class DashboardServiceImpl implements DashboardService {

        private final PatientRepository patientRepository;
        private final AppointmentRepository appointmentRepository;
        private final MedicineRepository medicineRepository;
        private final BillingRepository billingRepository;

        @Autowired
        public DashboardServiceImpl(
                        PatientRepository patientRepository,
                        AppointmentRepository appointmentRepository,
                        MedicineRepository medicineRepository,
                        BillingRepository billingRepository) {
                this.patientRepository = patientRepository;
                this.appointmentRepository = appointmentRepository;
                this.medicineRepository = medicineRepository;
                this.billingRepository = billingRepository;
        }

        @Override
        public DashboardSummaryDTO getSummary() {
                LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

                long totalPatients = patientRepository.count();
                long todayAppointments = appointmentRepository.countByAppointmentTimeBetween(startOfDay, endOfDay);
                long lowStock = medicineRepository.countLowStock();

                BigDecimal totalRevenue = billingRepository.sumTotalRevenue();
                if (totalRevenue == null)
                        totalRevenue = BigDecimal.ZERO;

                long inQueue = appointmentRepository.countByStatusInAndAppointmentTimeBetween(
                                List.of(AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_CONSULTATION),
                                startOfDay, endOfDay);

                long consultations = appointmentRepository.countByStatusAndUpdatedAtBetween(
                                AppointmentStatus.COMPLETED,
                                startOfDay, endOfDay);

                long totalConsultations = appointmentRepository.countByStatus(AppointmentStatus.COMPLETED);

                List<WeeklyStatisticsDTO> weeklyStats = java.util.stream.IntStream.rangeClosed(-3, 3)
                        .mapToObj(i -> {
                            LocalDate date = LocalDate.now().plusDays(i);
                            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
                            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
                            return WeeklyStatisticsDTO.builder()
                                    .day(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                                    .appointments(appointmentRepository.countByAppointmentTimeBetween(start, end))
                                    .patients(patientRepository.countByCreatedAtBetween(start, end))
                                    .build();
                        })
                        .toList();

                // Generate department-wise stats
                List<DepartmentStatisticsDTO> deptStats = java.util.Arrays.stream(Department.values())
                        .map(dept -> new java.util.AbstractMap.SimpleEntry<>(dept, appointmentRepository.countByDepartment(dept)))
                        .filter(entry -> entry.getValue() > 0)
                        .map(entry -> DepartmentStatisticsDTO.builder()
                                .department(entry.getKey().name())
                                .appointmentCount(entry.getValue())
                                .build())
                        .toList();

                return DashboardSummaryDTO.builder()
                                .totalPatients(totalPatients)
                                .todayAppointments(todayAppointments)
                                .lowStockMedicines(lowStock)
                                .totalRevenue(totalRevenue)
                                .patientsInQueue(inQueue)
                                .completedConsultations(consultations)
                                .totalCompletedConsultations(totalConsultations)
                                .weeklyStats(weeklyStats)
                                .departmentStats(deptStats)
                                .build();
        }
}
