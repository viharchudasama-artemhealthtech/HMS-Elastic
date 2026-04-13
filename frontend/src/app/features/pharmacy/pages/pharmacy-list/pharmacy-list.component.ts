import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CalendarModule } from 'primeng/calendar';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { TableModule } from 'primeng/table';
import { Subject, debounceTime, distinctUntilChanged, finalize, of, switchMap, takeUntil } from 'rxjs';
import { ApiResponse } from '../../../../core/models/common.models';
import { Medicine, MedicineCategory, MedicineSlice, MedicineSuggestion } from '../../../../core/models/pharmacy.models';
import { AuthService } from '../../../../core/services/auth.service';
import { InventoryService } from '../../../../core/services/inventory.service';
import { MedicineSearchService } from '../../../../core/services/medicine-search.service';
import { MedicineService } from '../../../../core/services/medicine.service';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';
import { createMedicineForm, createRestockForm } from '../../utils/pharmacy-list-form';
import {
  buildCategoryOptions,
  filterMedicinesByLowStock,
  formatMedicineDate,
  isMedicineLowStock,
} from '../../utils/pharmacy-list.utils';

@Component({
  selector: 'app-pharmacy-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SidebarComponent,
    HeaderComponent,
    InputTextModule,
    InputTextareaModule,
    InputNumberModule,
    DropdownModule,
    CalendarModule,
    TableModule,
  ],
  templateUrl: './pharmacy-list.component.html',
  styleUrl: './pharmacy-list.component.scss',
})
export class PharmacyListComponent implements OnInit, OnDestroy {
  medicines: Medicine[] = [];
  filteredMedicines: Medicine[] = [];
  searchMatchedMedicineIds = new Set<string>();
  searchControl = new FormControl('', { nonNullable: true });
  searchSuggestions: MedicineSuggestion[] = [];
  selectedSuggestion: MedicineSuggestion | null = null;
  isSearching = false;
  searchError = '';
  isLoading = true;
  userRole: string | null = null;
  showAddForm = false;
  showEditForm = false;
  showRestockForm = false;
  editingMedicine: Medicine | null = null;
  restockingMedicine: Medicine | null = null;
  medicineForm!: FormGroup;
  restockForm!: FormGroup;
  isSubmitting = false;
  isReindexing = false;
  errorMessage = '';
  successMessage = '';
  showLowStockOnly = false;
  minExpiryDate: Date = new Date();
  currentPage = 0;
  readonly pageSize = 10;
  hasNextPage = false;
  hasPreviousPage = false;

  categories = Object.values(MedicineCategory);
  private readonly destroy$ = new Subject<void>();

  constructor(
    private medicineService: MedicineService,
    private inventoryService: InventoryService,
    private medicineSearchService: MedicineSearchService,
    private authService: AuthService,
    private fb: FormBuilder,
  ) {}

  ngOnInit(): void {
    this.userRole = this.authService.getUserRole();
    this.initForm();
    this.calculateMinDate();
    this.initAutocomplete();
    this.loadMedicines();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  calculateMinDate(): void {
    const today = new Date();
    this.minExpiryDate = new Date(today.getFullYear(), today.getMonth(), today.getDate() + 1);
  }

  initForm(): void {
    this.medicineForm = createMedicineForm(this.fb);
    this.restockForm = createRestockForm(this.fb);
  }

  initAutocomplete(): void {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((value) => {
          const keyword = value.trim();
          this.searchError = '';
          this.selectedSuggestion = null;

          if (keyword.length < 2) {
            this.searchSuggestions = [];
            this.searchMatchedMedicineIds.clear();
            this.applyFilter();
            this.isSearching = false;
            return of(null);
          }

          this.isSearching = true;
          return this.medicineSearchService.search(keyword).pipe(
            finalize(() => {
              this.isSearching = false;
            }),
          );
        }),
        takeUntil(this.destroy$),
      )
      .subscribe({
        next: (response) => {
          this.searchSuggestions = response?.data || [];
          this.syncTableWithSearchResults(this.searchSuggestions);
        },
        error: (error: string) => {
          this.searchSuggestions = [];
          this.searchMatchedMedicineIds.clear();
          this.applyFilter();
          this.searchError = error || 'Unable to fetch medicine suggestions.';
        },
      });
  }

  loadMedicines(): void {
    this.isLoading = true;
    this.medicineService.getPaged(this.currentPage, this.pageSize).subscribe({
      next: (res: ApiResponse<MedicineSlice>) => {
        const pageData = res.data;
        this.medicines = pageData?.content || [];
        this.currentPage = pageData?.page ?? this.currentPage;
        this.hasNextPage = pageData?.hasNext ?? false;
        this.hasPreviousPage = pageData?.hasPrevious ?? false;
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  applyFilter(): void {
    const baseMedicines = filterMedicinesByLowStock(this.medicines, this.showLowStockOnly);
    const keyword = this.searchControl.value.trim();

    if (keyword.length >= 2) {
      this.filteredMedicines = baseMedicines.filter((medicine) =>
        this.searchMatchedMedicineIds.has(String(medicine.id)),
      );
      return;
    }

    this.filteredMedicines = baseMedicines;
  }

  toggleLowStock(): void {
    this.showLowStockOnly = !this.showLowStockOnly;
    this.applyFilter();
  }

  goToNextPage(): void {
    if (!this.hasNextPage || this.isLoading) {
      return;
    }

    this.currentPage += 1;
    this.loadMedicines();
  }

  goToPreviousPage(): void {
    if (!this.hasPreviousPage || this.isLoading) {
      return;
    }

    this.currentPage -= 1;
    this.loadMedicines();
  }

  reindexMedicines(): void {
    this.isReindexing = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.medicineSearchService.reindex().subscribe({
      next: (res) => {
        this.successMessage = res.message || 'Medicines reindexed successfully.';
        this.isReindexing = false;
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message || 'Reindex failed.';
        this.isReindexing = false;
      },
    });
  }

  selectSuggestion(suggestion: MedicineSuggestion): void {
    this.selectedSuggestion = suggestion;
    this.searchSuggestions = [];
    this.searchControl.setValue(suggestion.name, { emitEvent: false });
    this.searchMatchedMedicineIds = new Set([String(suggestion.id)]);
    this.applyFilter();
  }

  clearSearch(): void {
    this.searchControl.setValue('', { emitEvent: false });
    this.selectedSuggestion = null;
    this.searchSuggestions = [];
    this.searchMatchedMedicineIds.clear();
    this.applyFilter();
    this.searchError = '';
    this.isSearching = false;
  }

  private syncTableWithSearchResults(suggestions: MedicineSuggestion[]): void {
    this.searchMatchedMedicineIds = new Set(suggestions.map((s) => String(s.id)));
    this.applyFilter();
  }

  highlightMatch(name: string): string {
    const query = this.searchControl.value.trim();
    if (!query) {
      return this.escapeHtml(name);
    }

    const escapedQuery = query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const regex = new RegExp(`(${escapedQuery})`, 'ig');
    return this.escapeHtml(name).replace(regex, '<mark>$1</mark>');
  }

  private escapeHtml(value: string): string {
    return value
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  openAddForm(): void {
    this.showAddForm = true;
    this.showEditForm = false;
    this.showRestockForm = false;
    this.editingMedicine = null;
    this.restockingMedicine = null;
    this.medicineForm.reset({ unitPrice: null, quantityInStock: 0, reorderLevel: 10 });
    this.errorMessage = '';
    this.successMessage = '';
  }

  openEditForm(med: Medicine): void {
    this.editingMedicine = med;
    this.showEditForm = true;
    this.showAddForm = false;
    this.showRestockForm = false;
    this.restockingMedicine = null;
    this.medicineForm.patchValue({
      name: med.name,
      medicineCode: med.medicineCode,
      category: med.category,
      manufacturer: med.manufacturer,
      description: med.description,
      unitPrice: med.unitPrice,
      quantityInStock: med.quantityInStock,
      reorderLevel: med.reorderLevel,
      expiryDate: med.expiryDate ? new Date(med.expiryDate) : null,
    });
    this.errorMessage = '';
    this.successMessage = '';
  }

  openRestockForm(med: Medicine): void {
    this.restockingMedicine = med;
    this.showRestockForm = true;
    this.showAddForm = false;
    this.showEditForm = false;
    this.editingMedicine = null;
    this.restockForm.reset({ quantity: 10 });
    this.errorMessage = '';
    this.successMessage = '';
  }

  closeForm(): void {
    this.showAddForm = false;
    this.showEditForm = false;
    this.showRestockForm = false;
    this.editingMedicine = null;
    this.restockingMedicine = null;
  }

  onSubmit(): void {
    if (this.medicineForm.invalid) {
      this.medicineForm.markAllAsTouched();
      return;
    }
    this.isSubmitting = true;
    const data = this.medicineForm.value;
    const payload = {
      ...data,
      expiryDate: formatMedicineDate(data.expiryDate),
    };

    if (this.showEditForm && this.editingMedicine) {
      this.medicineService.update(this.editingMedicine.id, payload).subscribe({
        next: () => {
          this.successMessage = 'Medicine updated successfully!';
          this.isSubmitting = false;
          this.closeForm();
          this.loadMedicines();
        },
        error: (err: HttpErrorResponse) => {
          this.errorMessage = err.error?.message || 'Update failed.';
          this.isSubmitting = false;
        },
      });
    } else {
      this.medicineService.create(payload).subscribe({
        next: () => {
          this.successMessage = 'Medicine added successfully!';
          this.isSubmitting = false;
          this.closeForm();
          this.loadMedicines();
        },
        error: (err: HttpErrorResponse) => {
          this.errorMessage = err.error?.message || 'Create failed.';
          this.isSubmitting = false;
        },
      });
    }
  }

  onRestockSubmit(): void {
    if (this.restockForm.invalid || !this.restockingMedicine) {
      this.restockForm.markAllAsTouched();
      return;
    }
    this.isSubmitting = true;
    const quantity = this.restockForm.value.quantity;

    this.inventoryService.restock(this.restockingMedicine.id, quantity).subscribe({
      next: () => {
        this.successMessage = `Restocked ${this.restockingMedicine!.name} with ${quantity} units!`;
        this.isSubmitting = false;
        this.closeForm();
        this.loadMedicines();
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message || 'Restock failed.';
        this.isSubmitting = false;
      },
    });
  }

  onDelete(id: number): void {
    if (!confirm('Delete this medicine?')) return;
    this.medicineService.delete(id).subscribe({
      next: () => {
        this.loadMedicines();
      },
      error: () => {},
    });
  }

  isLowStock(med: Medicine): boolean {
    return isMedicineLowStock(med);
  }

  categoryOptions(): Array<{ label: string; value: string }> {
    return buildCategoryOptions(this.categories);
  }
}
