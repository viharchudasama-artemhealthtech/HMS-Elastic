import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { Observable } from 'rxjs';
import { BOOKABLE_DEPARTMENTS, formatDepartmentLabel } from '../../../../core/constants/department.constants';
import { ApiResponse } from '../../../../core/models/common.models';
import { Doctor, DoctorOnboardingResponse, DoctorRegistrationRequest } from '../../../../core/models/doctor.models';
import { DoctorService } from '../../../../core/services/doctor.service';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';
import { createDoctorRegistrationForm } from '../../utils/doctor-registration-form';
import { buildDoctorDepartmentOptions, buildDoctorFormPatch } from '../../utils/doctor-registration.utils';

@Component({
  selector: 'app-doctor-registration',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    SidebarComponent,
    HeaderComponent,
    InputTextModule,
    InputTextareaModule,
    InputNumberModule,
    DropdownModule,
  ],
  templateUrl: './doctor-registration.component.html',
  styleUrl: './doctor-registration.component.scss',
})
export class DoctorRegistrationComponent implements OnInit {
  doctorForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  departments = BOOKABLE_DEPARTMENTS;
  isEditMode = false;
  isViewMode = false;
  doctorId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private doctorService: DoctorService,
    private router: Router,
    private elementRef: ElementRef<HTMLElement>,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.doctorForm = createDoctorRegistrationForm(this.fb);

    this.route.queryParams.subscribe((params) => {
      this.doctorId = params['doctorId'] ? Number(params['doctorId']) : null;
      this.isViewMode = params['mode'] === 'view';
      this.isEditMode = params['mode'] === 'edit' && !!this.doctorId;

      if (this.doctorId) {
        this.loadDoctor(this.doctorId);
      } else {
        this.doctorForm.enable();
      }
    });
  }

  getDepartmentLabel(department: string): string {
    return formatDepartmentLabel(department);
  }

  departmentOptions(): Array<{ label: string; value: string }> {
    return buildDoctorDepartmentOptions(this.departments);
  }

  onSubmit(): void {
    if (this.isViewMode) {
      return;
    }

    if (this.doctorForm.invalid) {
      this.errorMessage = 'Please complete all required doctor account details before submitting.';
      this.doctorForm.markAllAsTouched();
      this.scrollToFirstInvalidField();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    const payload = {
      ...this.doctorForm.value,
      isAvailable: true,
    } as DoctorRegistrationRequest;

    const request$: Observable<ApiResponse<DoctorOnboardingResponse | Doctor>> =
      this.isEditMode && this.doctorId
        ? this.doctorService.update(this.doctorId!, payload)
        : this.doctorService.register(payload);

    request$.subscribe({
      next: (res: ApiResponse<DoctorOnboardingResponse | Doctor>) => {
        this.isLoading = false;
        if (this.isEditMode) {
          this.successMessage = 'Doctor profile updated successfully.';
        } else {
          const onboarding = res.data as DoctorOnboardingResponse;
          this.successMessage = `Doctor registered. Username: ${onboarding.username} | Temporary password: ${onboarding.temporaryPassword}`;
        }
        setTimeout(() => {
          this.router.navigate(['/staff']);
        }, 2000);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage =
          err.error?.message || `Failed to ${this.isEditMode ? 'update' : 'register'} doctor. Please try again.`;
        this.isLoading = false;
      },
    });
  }

  private scrollToFirstInvalidField(): void {
    setTimeout(() => {
      const invalidControl = this.elementRef.nativeElement.querySelector(
        '.ng-invalid[formControlName], .ng-invalid .p-inputtext, .ng-invalid .p-dropdown-label',
      ) as HTMLElement | null;

      invalidControl?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      invalidControl?.focus?.();
    });
  }

  private loadDoctor(id: number): void {
    this.isLoading = true;
    this.doctorService.getById(id).subscribe({
      next: (res: ApiResponse<Doctor>) => {
        const doctor = res.data;
        this.doctorForm.patchValue(buildDoctorFormPatch(doctor)); // patchValue allows partial updates, so we can skip disabled fields

        if (this.isViewMode) {
          this.doctorForm.disable();
        } else {
          this.doctorForm.enable();
          this.doctorForm.get('username')?.disable();
          this.doctorForm.get('temporaryPassword')?.disable();
        }

        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load doctor details.';
        this.isLoading = false;
      },
    });
  }
}
