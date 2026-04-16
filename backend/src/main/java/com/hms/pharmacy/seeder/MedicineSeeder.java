package com.hms.pharmacy.seeder;

import com.hms.pharmacy.entity.Medicine;
import com.hms.pharmacy.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicineSeeder implements CommandLineRunner {

    private final MedicineRepository medicineRepository;
    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        // Skip seeding if medicines already exist
        if (medicineRepository.count() > 0) {
            log.info("Medicines already exist, skipping seeding");
            return;
        }

        String csvPath = "../Drug Data.csv";
        List<String> lines;

        try {
            lines = Files.readAllLines(Paths.get(csvPath));
        } catch (Exception e) {
            log.error("Failed to read CSV file: {}", e.getMessage());
            return;
        }

        int successCount = 0;
        int errorCount = 0;

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            String medicineName = line.trim();
            // Remove leading and trailing double quotes if present
            if (medicineName.startsWith("\"") && medicineName.endsWith("\"")) {
                medicineName = medicineName.substring(1, medicineName.length() - 1);
            }

            try {
                Medicine medicine = Medicine.builder()
                        .name(medicineName)
                        .medicineCode(generateRandomMedicineCode())
                        .description(generateRandomDescription())
                        .category(generateRandomCategory())
                        .manufacturer(generateRandomManufacturer())
                        .expiryDate(generateRandomExpiryDate())
                        .quantityInStock(random.nextInt(1001)) // 0-1000
                        .unitPrice(BigDecimal.valueOf(random.nextDouble() * 999 + 1).setScale(2, BigDecimal.ROUND_HALF_UP))
                        .reorderLevel(random.nextInt(100) + 1) // 1-100
                        .dosage(generateRandomDosage())
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .createdBy("admin")
                        .updatedBy("admin")
                        .build();

                medicineRepository.save(medicine);
                successCount++;
                log.info("Inserted medicine: {}", medicineName);
            } catch (Exception e) {
                errorCount++;
                log.error("Failed to insert medicine '{}': {}", medicineName, e.getMessage());
            }
        }

        log.info("Seeding completed. Success: {}, Errors: {}", successCount, errorCount);
    }

    private String generateRandomMedicineCode() {
        return "MED-" + (10000 + random.nextInt(90000)); // MED-10000 to MED-99999
    }

    private String generateRandomDescription() {
        String[] descriptions = {"Pain relief tablet", "Antibiotic capsule", "Vitamin supplement", "Cardiac medicine", "Diabetic drug", "Antiseptic solution"};
        return descriptions[random.nextInt(descriptions.length)];
    }

    private com.hms.common.enums.MedicineCategory generateRandomCategory() {
        com.hms.common.enums.MedicineCategory[] categories = com.hms.common.enums.MedicineCategory.values();
        return categories[random.nextInt(categories.length)];
    }

    private String generateRandomManufacturer() {
        String[] manufacturers = {"Sun Pharma", "Cipla", "Dr. Reddy's", "Pfizer", "GSK", "AstraZeneca"};
        return manufacturers[random.nextInt(manufacturers.length)];
    }

    private LocalDate generateRandomExpiryDate() {
        LocalDate now = LocalDate.now();
        int daysToAdd = random.nextInt(365 * 3) + 30; // 30 days to 3 years from now
        return now.plusDays(daysToAdd);
    }

    private String generateRandomDosage() {
        String[] dosages = {"500mg", "200mg", "1000mg", "250mg", "10ml", "5ml"};
        return dosages[random.nextInt(dosages.length)];
    }
}