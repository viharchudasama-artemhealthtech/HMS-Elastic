# Inefficient List/Array Operations Report

Comprehensive analysis of unnecessary list operations, intermediate collections, and optimization opportunities in both Java backend and TypeScript/Angular frontend.

---

## JAVA BACKEND - Inefficient List Operations

### Category 1: Using `Collectors.toList()` instead of `.toList()` (Java 16+)

These can be replaced with the newer, more concise `.toList()` method available in Java 16+.

#### 1. [UserServiceImpl.java](backend/src/main/java/com/hms/user/service/impl/UserServiceImpl.java#L26-L28)

**Lines: 26-28**

```java
return userRepository.findAll().stream()
        .map(userMapper::toResponseDTO)
        .collect(Collectors.toList());
```

**Issue:** Using deprecated `Collectors.toList()` in Java 16+
**Suggestion:** Replace with `.toList()` for cleaner, immutable list

```java
return userRepository.findAll().stream()
        .map(userMapper::toResponseDTO)
        .toList();
```

#### 2. [GlobalExceptionHandler.java](backend/src/main/java/com/hms/common/exception/GlobalExceptionHandler.java#L164-L166)

**Lines: 164-166**

```java
List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> new ValidationError(e.getField(), e.getDefaultMessage()))
        .collect(Collectors.toList());
```

**Suggestion:** Replace with `.toList()`

```java
List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> new ValidationError(e.getField(), e.getDefaultMessage()))
        .toList();
```

---

### Category 2: Mutable ArrayList Creation with Subsequent Loop Additions

These patterns create empty ArrayLists and then add items one-by-one in loops. This is inefficient compared to directly building the list via streams.

#### 3. [DashboardServiceImpl.java](backend/src/main/java/com/hms/dashboard/service/impl/DashboardServiceImpl.java#L68-L80)

**Lines: 68-80**

```java
List<WeeklyStatisticsDTO> weeklyStats = new ArrayList<>();
// Generate a 7-day window centered on today (-3 to +3 days)
for (int i = -3; i <= 3; i++) {
    LocalDate date = LocalDate.now().plusDays(i);
    LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
    LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

    weeklyStats.add(WeeklyStatisticsDTO.builder()
                    .day(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .appointments(appointmentRepository.countByAppointmentTimeBetween(start, end))
                    .patients(patientRepository.countByCreatedAtBetween(start, end))
                    .build());
}
```

**Issue:** Creates empty list and adds items in loop. Can be built more efficiently.
**Suggestion:** Use functional approach with streams or Java 17+ records:

```java
List<WeeklyStatisticsDTO> weeklyStats = IntStream.rangeClosed(-3, 3)
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
```

#### 4. [DashboardServiceImpl.java](backend/src/main/java/com/hms/dashboard/service/impl/DashboardServiceImpl.java#L83-L90)

**Lines: 83-90**

```java
List<DepartmentStatisticsDTO> deptStats = new ArrayList<>();
for (Department dept : Department.values()) {
        long count = appointmentRepository.countByDepartment(dept);
        if (count > 0) {
                deptStats.add(DepartmentStatisticsDTO.builder()
                                .department(dept.name())
                                .appointmentCount(count)
                                .build());
        }
}
```

**Issue:** Creates empty list and adds items in loop with conditional check.
**Suggestion:** Use stream with filter:

```java
List<DepartmentStatisticsDTO> deptStats = Arrays.stream(Department.values())
        .map(dept -> {
            long count = appointmentRepository.countByDepartment(dept);
            return new AbstractMap.SimpleEntry<>(dept, count);
        })
        .filter(entry -> entry.getValue() > 0)
        .map(entry -> DepartmentStatisticsDTO.builder()
                .department(entry.getKey().name())
                .appointmentCount(entry.getValue())
                .build())
        .toList();
```

---

### Category 3: Unnecessary Intermediate Collections

#### 5. [MedicineSearchService.java](backend/src/main/java/com/hms/pharmacy/service/search/MedicineSearchService.java#L81-L83)

**Lines: 81-83**

```java
.terms(values -> values.value(phoneticCodes.stream()
        .map(FieldValue::of)
        .toList()))
```

**Issue:** Creates intermediate list just to pass to terms() method. Can be passed as stream directly in some cases.
**Suggestion:** Many search APIs accept iterables/streams directly. Consider:

```java
.terms(values -> values.value(phoneticCodes.stream()
        .map(FieldValue::of)
        .collect(Collectors.toList())))  // Only if necessary for the API
```

#### 6. [MedicineSearchService.java](backend/src/main/java/com/hms/pharmacy/service/search/MedicineSearchService.java#L127)

**Lines: 127**

```java
List<MedicineSearch> docs = medicines.stream().map(this::toSearchDocument).toList();
```

**Issue:** Creates intermediate list `docs` that is immediately used once (saveAll). Consider passing stream directly if repository supports it.
**Suggestion:** Check if `medicineSearchRepository.saveAll()` can accept collection streams:

```java
medicineSearchRepository.saveAll(medicines.stream()
        .map(this::toSearchDocument)
        .toList());
```

#### 7. [MedicineSearchService.java](backend/src/main/java/com/hms/pharmacy/service/search/MedicineSearchService.java#L46-L53)

**Lines: 46-53**

```java
return docs.stream()
        .map(MedicineSearch::getId)
        .map(medicineRepository::findById)
        .filter(java.util.Optional::isPresent)
        .map(java.util.Optional::get)
        .map(medicineMapper::toDto)
        .toList();
```

**Issue:** Multiple successive `.map()` operations. The `map(findById).filter(isPresent).map(get)` pattern is inefficient.
**Suggestion:** Use `flatMap` with Optional:

```java
return docs.stream()
        .mapToLong(MedicineSearch::getId)
        .mapToObj(medicineRepository::findById)
        .flatMap(java.util.Optional::stream)
        .map(medicineMapper::toDto)
        .toList();
```

---

### Category 4: Stream Operations That Can Be Optimized

#### 8. [UserServiceImpl.java](backend/src/main/java/com/hms/user/service/impl/UserServiceImpl.java#L26-L28)

**Lines: 26-28**
**Issue:** Generic .findAll().stream() pattern used repeatedly across services
**Suggestion:** Consider using repository projections or queries that directly return DTOs if available.

#### 9. [MedicineServiceImpl.java](backend/src/main/java/com/hms/pharmacy/service/impl/MedicineServiceImpl.java#L186-L200)

**Lines: 186-200**

```java
return inventoryTransactionRepository.findAll().stream()
        .map(t -> InventoryTransactionResponseDTO.builder()
                .id(t.getId())
                .medicineCode(t.getMedicine().getMedicineCode())
                .medicined(t.getMedicine().getName())
                .transactionType(t.getTransactionType())
                .quantity(t.getQuantity())
                .referenceId(t.getReferenceId())
                .notes(t.getNotes())
                .createdAt(t.getCreatedAt())
                .createdBy(t.getCreatedBy())
                .build())
        .toList();
```

**Issue:** Loads entire table into memory before mapping.
**Suggestion:** If this supports pagination, use it. Otherwise, use .toList() instead of .collect(Collectors.toList())

#### 10. [MedicineSearchService.java](backend/src/main/java/com/hms/pharmacy/service/search/MedicineSearchService.java#L150-L166)

**Lines: 150-166**

```java
private List<String> buildPhoneticCodes(String value) {
    if (value == null || value.isBlank()) {
        return List.of();
    }

    Set<String> tokens = Arrays.stream(value.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
            .filter(token -> !token.isBlank())
            .collect(Collectors.toSet());
    tokens.add(value.toLowerCase(Locale.ROOT).trim());

    return tokens.stream()
            .flatMap(token -> Arrays.stream(new String[]{
                    doubleMetaphone.doubleMetaphone(token),
                    doubleMetaphone.doubleMetaphone(token, true)
            }))
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(code -> !code.isBlank())
            .distinct()
            .toList();
}
```

**Issue:** Creates intermediate Set, then streams it again. Also uses `collect(Collectors.toSet())` instead of `.toList()` or direct Set operations.
**Suggestion:** Combine operations to reduce intermediate collections:

```java
private List<String> buildPhoneticCodes(String value) {
    if (value == null || value.isBlank()) {
        return List.of();
    }

    String lowerValue = value.toLowerCase(Locale.ROOT);
    return Arrays.stream(lowerValue.split("[^a-z0-9]+"))
            .filter(token -> !token.isBlank())
            .flatMap(token -> Stream.of(
                    doubleMetaphone.doubleMetaphone(token),
                    doubleMetaphone.doubleMetaphone(token, true)
            ))
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(code -> !code.isBlank())
            .distinct()
            .sorted()
            .toList();
}
```

#### 11. [BillingServiceImpl.java](backend/src/main/java/com/hms/billing/service/impl/BillingServiceImpl.java#L243-L245)

**Lines: 243-245**

```java
BigDecimal subtotal = billing.getItems().stream()
        .map(i -> i.getTotalValue() != null ? i.getTotalValue() : BigDecimal.ZERO)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
```

**Issue:** Stream operation for simple summation. Consider using Apache Commons or custom utility.
**Suggestion:** Keep as is if no performance issue, but consider:

```java
BigDecimal subtotal = billing.getItems().stream()
        .map(BillingItem::getTotalValue)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
// Or if null handling is needed:
BigDecimal subtotal = billing.getItems().parallelStream()
        .map(i -> Optional.ofNullable(i.getTotalValue()).orElse(BigDecimal.ZERO))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
```

#### 12. [AppointmentServiceImpl.java](backend/src/main/java/com/hms/appointment/service/impl/AppointmentServiceImpl.java#L186-L192)

**Lines: 186-192**

```java
if (doctorConflicts.stream().anyMatch(a -> !a.getId().equals(currentAppointmentId))) {
    throw new SlotAlreadyBookedException("Doctor is already booked for " + time);
}

List<Appointment> patientConflicts = appointmentRepository.findAndLockPatientConflictingAppointments(patientId,
        time, ACTIVE_STATUSES);
if (patientConflicts.stream().anyMatch(a -> !a.getId().equals(currentAppointmentId))) {
    throw new SlotAlreadyBookedException("Patient already has an appointment for " + time);
}
```

**Issue:** Using `.anyMatch()` on lists that may contain specific items. If list is expected to be small, check size first.
**Suggestion:** Add size check to avoid unnecessary stream processing:

```java
if (!doctorConflicts.isEmpty() && doctorConflicts.stream()
        .anyMatch(a -> !a.getId().equals(currentAppointmentId))) {
    throw new SlotAlreadyBookedException("Doctor is already booked for " + time);
}
```

---

### Category 5: Entity Field Initialization with ArrayList

#### 13. [Prescription.java](backend/src/main/java/com/hms/prescription/entity/Prescription.java#L43)

**Lines: 43**

```java
@OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
@Builder.Default
private List<PrescriptionMedicine> medicines = new ArrayList<>();
```

**Note:** This is correct usage for JPA entities. ArrayList initialization is necessary for Hibernate to manage the collection lifecycle.

#### 14. [Billing.java](backend/src/main/java/com/hms/billing/entity/Billing.java#L71)

**Lines: 71**

```java
@OneToMany(mappedBy = "billing", cascade = CascadeType.ALL, orphanRemoval = true)
@Builder.Default
private List<BillingItem> items = new ArrayList<>();
```

**Note:** Same as above - this is correct for JPA entity management.

#### 15. [BillingServiceImpl.java](backend/src/main/java/com/hms/billing/service/impl/BillingServiceImpl.java#L195)

**Lines: 195**

```java
billing.setItems(new ArrayList<>());
```

**Issue:** Creating empty ArrayList to clear items. Questionable design - should use `.clear()` or provide a method.
**Suggestion:** Use clear() method:

```java
billing.getItems().clear();
```

---

## TYPESCRIPT/ANGULAR FRONTEND - Inefficient Array Operations

### Category 1: Mutable Array Operations (push/splice/removeAt)

#### 16. [PrescriptionCreateComponent.ts](frontend/src/app/features/prescription/pages/prescription-create/prescription-create.component.ts#L122-L123)

**Lines: 122-123**

```typescript
addMedicine(): void {
    this.medicines.push(this.createMedicineGroup());
    this.medicineSuggestionsByRow.push([]);
    this.ensureMedicineSearchStream(this.medicines.length - 1);
}
```

**Issue:** Using `.push()` on FormArray. While acceptable for FormArray, consider immutable patterns for data arrays.
**Suggestion:** Document if this must be mutable. If possible, use immutable operations.

#### 17. [PrescriptionCreateComponent.ts](frontend/src/app/features/prescription/pages/prescription-create/prescription-create.component.ts#L127-L129)

**Lines: 127-129**

```typescript
removeMedicine(index: number): void {
    if (this.medicines.length > 1) {
        this.medicines.removeAt(index);
        this.medicineSuggestionsByRow.splice(index, 1);
```

**Issue:** Using `splice()` which mutates array in place. Should use immutable operations.
**Suggestion:** If not using FormArray:

```typescript
this.medicineSuggestionsByRow = this.medicineSuggestionsByRow
  .slice(0, index)
  .concat(this.medicineSuggestionsByRow.slice(index + 1));
```

#### 18. [BillingListComponent.ts](frontend/src/app/features/billing/billing-list/billing-list.component.ts#L113)

**Lines: 113**

```typescript
addItem(): void {
    this.items.push(this.createItemGroup());
}
```

**Issue:** Same as #16 - using push on

FormArray. This is acceptable for FormArray but should be documented.

#### 19. [BillingListComponent.ts](frontend/src/app/features/billing/billing-list/billing-list.component.ts#L201)

**Lines: 201**

```typescript
this.items.push(group);
```

**Issue:** Mutable operation in component logic.
**Suggestion:** Consider immutable update when logic is not tied to FormArray.

---

### Category 2: Unnecessary .map() Creating New Arrays

#### 20. [PharmacyListComponent.ts](frontend/src/app/features/pharmacy/pages/pharmacy-list/pharmacy-list.component.ts#L235)

**Lines: 235**

```typescript
this.searchMatchedMedicineIds = new Set(
  suggestions.map((suggestion) => String(suggestion.id)),
);
```

**Issue:** Creates intermediate array from map, then converts to Set.
**Suggestion:** Create Set directly or use map before Set:

```typescript
this.searchMatchedMedicineIds = new Set(suggestions.map((s) => String(s.id)));
// Or more efficiently:
this.searchMatchedMedicineIds = new Set(
  Array.from(suggestions, (s) => String(s.id)),
);
```

#### 21. [DashboardUtils.ts](frontend/src/app/features/dashboard/utils/dashboard.utils.ts#L85-L87)

**Lines: 85-87**

```typescript
const labels = stats.map((s) => s.day);
const appointmentData = stats.map((s) => s.appointments || 0);
const patientData = stats.map((s) => s.patients || 0);
```

**Issue:** Three separate `.map()` calls on same array creating three new arrays.
**Suggestion:** Combine into single pass:

```typescript
const { labels, appointmentData, patientData } = stats.reduce(
  (acc, s) => {
    acc.labels.push(s.day);
    acc.appointmentData.push(s.appointments || 0);
    acc.patientData.push(s.patients || 0);
    return acc;
  },
  { labels: [], appointmentData: [], patientData: [] },
);
```

#### 22. [DashboardUtils.ts](frontend/src/app/features/dashboard/utils/dashboard.utils.ts#L142)

**Lines: 142**

```typescript
const labels = stats.map((s) =>
```

**Issue:** Additional map operations on charts - similar pattern.

#### 23. [DashboardUtils.ts](frontend/src/app/features/dashboard/utils/dashboard.utils.ts#L148)

**Lines: 148**

```typescript
const counts = stats.map((s) => s.appointmentCount);
```

**Issue:** Unnecessary map creating new array.

---

### Category 3: Array Operations in Utility Functions

#### 24. [BillingDataUtils.ts](frontend/src/app/features/billing/utils/billing-data.utils.ts#L22-L23)

**Lines: 22-23**

```typescript
return appointments
  .filter(
    (appointment) =>
      appointment.status === "COMPLETED" || appointment.status === "CHECKED_IN",
  )
  .map((appointment) => ({
    ...appointment,
    label: `${new Date(appointment.appointmentTime).toLocaleDateString()} - ${appointment.department}`,
  }));
```

**Note:** This is reasonable - filter then map pattern is standard. Performance is good for typical appointment arrays.

#### 25. [BillingDataUtils.ts](frontend/src/app/features/billing/utils/billing-data.utils.ts#L41)

**Lines: 41**

```typescript
items: items.controls.map((control) => {
    const value = control.value;
    return {
        ...value,
        totalValue: (value.quantity || 0) * (value.unitPrice || 0),
    };
}),
```

**Issue:** Creates new array from FormArray.controls without checking if necessary.
**Suggestion:** Acceptable if needed for DTO, but consider caching result if called multiple times.

#### 26. [PdfExportService.ts](frontend/src/app/core/services/pdf-export.service.ts#L55)

**Lines: 55**

```typescript
const tableData = billing.items.map((item, index) => [
  index + 1,
  item.itemName,
  item.quantity,
  `INR ${this.decimalPipe.transform(item.unitPrice, "1.2-2")}`,
  `INR ${this.decimalPipe.transform(item.totalValue, "1.2-2")}`,
]);
```

**Issue:** Map is necessary here for table formatting, but be aware of pipe performance being called in map.
**Suggestion:** Consider memoizing decimal pipe calls if this is called frequently.

---

### Category 4: Utility Function Array Operations

#### 27. [PharmacyListUtils.ts](frontend/src/app/features/pharmacy/utils/pharmacy-list.utils.ts#L8)

**Lines: 8**

```typescript
return medicines.filter(
  (medicine) => medicine.quantityInStock <= medicine.reorderLevel,
);
```

**Issue:** Creates new array from filter even when showLowStockOnly is true.
**Note:** This is actually optimized - filter is only called when needed based on the condition.

#### 28. [DoctorRegistrationUtils.ts](frontend/src/app/features/staff/utils/doctor-registration.utils.ts#L6)

**Lines: 6**

```typescript
return departments.map((department) => ({
  label: formatDepartmentLabel(department),
  value: department,
}));
```

**Issue:** Creates new array with transformed objects. This is necessary for dropdown options.
**Note:** This is reasonable - object creation is necessary for UI binding.

#### 29. [AppointmentBookingUtils.ts](frontend/src/app/features/appointments/utils/appointment-booking.utils.ts#L30-L35)

**Lines: 30-35**

```typescript
return doctors
  .filter((doctor) => doctor.department === department && doctor.isAvailable)
  .sort((left, right) =>
    `${left.firstName} ${left.lastName}`.localeCompare(
      `${right.firstName} ${right.lastName}`,
    ),
  );
```

**Issue:** Creates new sorted array from filter. String concatenation in sort comparator is expensive.
**Suggestion:** Pre-calculate full names or use comparison factory:

```typescript
return doctors
  .filter((doctor) => doctor.department === department && doctor.isAvailable)
  .sort((a, b) => {
    const nameA = `${a.firstName} ${a.lastName}`;
    const nameB = `${b.firstName} ${b.lastName}`;
    return nameA.localeCompare(nameB);
  });
```

#### 30. [AppointmentBookingUtils.ts](frontend/src/app/features/appointments/utils/appointment-booking.utils.ts#L11-L18)

**Lines: 11-18**

```typescript
return patients.map((patient) => ({
  label: `${patient.name} (ID: ${patient.id})`,
  value: patient.id,
}));

export function buildDepartmentOptions(
  departments: string[],
): Array<SelectOption<string>> {
  return departments.map((department) => ({
    label: formatDepartmentLabel(department),
    value: department,
  }));
}
```

**Note:** These are reasonable - object transformation is necessary for UI binding.

#### 31. [PrescriptionUtilUtils.ts](frontend/src/app/features/prescription/utils/prescription-create.utils.ts#L7)

**Lines: 7**

```typescript
return medicines.filter(
  (medicine) =>
    medicine.name.toLowerCase().includes(normalizedQuery) ||
    medicine.medicineCode?.toLowerCase().includes(normalizedQuery),
);
```

**Issue:** Calls `.toLowerCase()` on each comparison. Should normalize before filtering.
**Suggestion:** Normalize query and filter more efficiently:

```typescript
const normalizedQuery = query.toLowerCase();
return medicines.filter(
  (medicine) =>
    medicine.name.toLowerCase().includes(normalizedQuery) ||
    medicine.medicineCode?.toLowerCase().includes(normalizedQuery),
);
```

#### 32. [AuthFormUtils.ts](frontend/src/app/features/auth/auth-form.utils.ts#L50)

**Lines: 50**

```typescript
return roles.map((role) => ({
  label: role.replace(/_/g, " "),
  value: role,
}));
```

**Note:** Reasonable - formatting is necessary for UI.

---

### Category 5: Array Operations with Slice

#### 33. [DashboardUtils.ts](frontend/src/app/features/dashboard/utils/dashboard.utils.ts#L163)

**Lines: 163**

```typescript
backgroundColor: colors.slice(0, stats.length),
```

**Issue:** `.slice()` creates new array unnecessarily.
**Suggestion:** Only slice if needed for limits:

```typescript
backgroundColor: colors.length <= stats.length ? colors : colors.slice(0, stats.length),
```

---

### Category 6: Complex Data Transformations

#### 34. [BillingDataUtils.ts](frontend/src/app/features/billing/utils/billing-data.utils.ts#L13)

**Lines: 13**

```typescript
return items.controls.reduce(
  (sum, _, index) => sum + getBillingItemTotal(items, index),
  0,
);
```

**Note:** Using reduce for summation - acceptable. Could use `sum()` pattern if available.

#### 35. [PrescriptionFormUtils.ts](frontend/src/app/features/prescription/utils/prescription-create-form.ts#L54)

**Lines: 54**

```typescript
const dosesPerDay = dosesArray
  ? dosesArray.reduce((acc: number, val: string) => acc + parseInt(val, 10), 0)
  : 0;
```

**Note:** Using reduce for summation - acceptable. Could optimize with early exit if non-numeric value found.

---

## SUMMARY OF FINDINGS

### High Priority Issues (Performance Impact)

| #   | File                       | Issue                                   | Type                |
| --- | -------------------------- | --------------------------------------- | ------------------- |
| 1   | MedicineSearchService.java | Multiple `.map()` with findById pattern | Java - Stream Op    |
| 2   | DashboardServiceImpl.java  | ArrayList + loop patterns (2 instances) | Java - Mutable List |
| 3   | BillingDataUtils.ts        | Multiple `.map()` on same stats array   | TS - Array Ops      |
| 4   | PrescriptionCreateUtils.ts | String operations in filter predicate   | TS - Array Filter   |
| 5   | PharmacyListComponent.ts   | Map then Set conversion                 | TS - Array Ops      |

### Medium Priority Issues (Code Quality)

| #   | File                        | Issue                                          | Type            |
| --- | --------------------------- | ---------------------------------------------- | --------------- |
| 6   | UserServiceImpl.java        | Collectors.toList() instead of .toList()       | Java - Syntax   |
| 7   | GlobalExceptionHandler.java | Collectors.toList() instead of .toList()       | Java - Syntax   |
| 8   | AppointmentBookingUtils.ts  | String concat in sort comparator               | TS - Array Sort |
| 9   | BillingServiceImpl.java     | setItems(new ArrayList<>()) instead of clear() | Java - Design   |
| 10  | DashboardUtils.ts           | .slice unnecessary condition                   | TS - Array Ops  |

### Low Priority Issues (Acceptable Patterns)

- Entity initialization with ArrayList (necessary for JPA)
- Map operations for UI binding (necessary for Angular)
- Filter-then-map patterns (standard functional approach)
- FormArray mutable operations (required by Angular framework)

---

## RECOMMENDATIONS

### Backend (Java)

1. **Update to Java 17+**: Use `.toList()` instead of `Collectors.toList()` across the codebase
2. **Refactor Dashboard Stats**: Replace ArrayList loops with Stream operations
3. **Optimize MedicineSearchService**: Simplify complex stream chains with flatMap
4. **Add Stream utilities**: Create custom collectors for common patterns

### Frontend (TypeScript/Angular)

1. **Combine multiple maps**: Reduce separate `.map()` calls to single pass with reduce/Object destructuring
2. **Immutable operations**: Use `.slice()` instead of `.splice()` where applicable
3. **Normalize before filtering**: Pre-normalize search strings before filter operations
4. **Cache expensive operations**: Consider memoizing pipe calls in maps

---

## TOTAL FINDINGS: 35 Instances Identified

- **Java Backend**: 15 instances
- **TypeScript/Angular Frontend**: 20 instances
- **High Priority**: 5 issues (significant performance concern)
- **Medium Priority**: 5 issues (code quality concerns)
- **Low Priority**: 25 issues (acceptable but could be improved)
