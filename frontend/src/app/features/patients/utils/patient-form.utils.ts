import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BloodGroup, Patient, UrgencyLevel } from '../../../core/models/patient.models';
import { FULL_NAME_PATTERN, PHONE_PATTERN, trimRequired } from '../../../core/validators/app-validators';

export function createPatientRegistrationForm(fb: FormBuilder): FormGroup {
  return fb.group({
    name: ['', [...trimRequired(2, 200), Validators.pattern(FULL_NAME_PATTERN)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    age: [null, [Validators.required, Validators.min(0), Validators.max(120)]],
    bloodGroup: [BloodGroup.O_POSITIVE, [Validators.required]],
    contactNumber: ['', [Validators.required, Validators.pattern(PHONE_PATTERN)]],
    urgencyLevel: [UrgencyLevel.LOW, [Validators.required]],
    prescription: ['', [...trimRequired(2, 2000)]],
    dose: ['', [...trimRequired(1, 500)]],
    fees: [null, [Validators.required, Validators.min(0.01)]],
  });
}

export function buildBloodGroupOptions(bloodGroups: string[]): Array<{ label: string; value: string }> {
  return bloodGroups.map((group) => ({
    label: group.replace('_POSITIVE', '+').replace('_NEGATIVE', '-'),
    value: group,
  }));
}

export function buildUrgencyOptions(levels: string[]): Array<{ label: string; value: string }> {
  return levels.map((level) => ({
    label: level,
    value: level,
  }));
}

export function getUrgencyClass(level: string): string {
  return `urgency-${level.toLowerCase()}`;
}

export function buildPatientRegistrationSuccessRoute(isEditMode: boolean,patient: Patient,): { path: string[]; queryParams?: Record<string, string | number> } {
  if (!isEditMode && patient.id) {
    return {
      path: ['/appointments/book'],
      queryParams: { patientId: patient.id },
    };
  }

  return {
    path: ['/patients'],
    queryParams: { registered: 'true' },
  };
}
