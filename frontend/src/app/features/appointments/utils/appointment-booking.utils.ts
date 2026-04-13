import { formatDepartmentLabel } from '../../../core/constants/department.constants';
import { Doctor } from '../../../core/models/doctor.models';
import { Patient } from '../../../core/models/patient.models';

export interface SelectOption<T> {
  label: string;
  value: T;
}

export function buildPatientOptions(patients: Patient[]): Array<SelectOption<number>> {
  return patients.map((patient) => ({
    label: `${patient.name} (ID: ${patient.id})`,
    value: patient.id,
  }));
}

export function buildDepartmentOptions(departments: string[]): Array<SelectOption<string>> {
  return departments.map((department) => ({
    label: formatDepartmentLabel(department),
    value: department,
  }));
}

export function filterAvailableDoctors(doctors: Doctor[], department: string): Doctor[] {
  if (!department) {
    return [];
  }

  return doctors
    .filter((doctor) => doctor.department === department && doctor.isAvailable)
    .sort((left, right) => {
      const nameLeft = `${left.firstName} ${left.lastName}`;
      const nameRight = `${right.firstName} ${right.lastName}`;
      return nameLeft.localeCompare(nameRight);
    });
}

export function buildDoctorOptions(doctors: Doctor[]): Array<SelectOption<number>> {
  return doctors.map((doctor) => ({
    label: `Dr. ${doctor.firstName} ${doctor.lastName} - ${doctor.specialization}`,
    value: doctor.id,
  }));
}

export function formatDateOnly(value: Date | null): string | null {
  if (!value) {
    return null;
  }

  const year = value.getFullYear();
  const month = String(value.getMonth() + 1).padStart(2, '0');
  const day = String(value.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function formatTimeOnly(value: Date | null): string | null {
  if (!value) {
    return null;
  }

  const hours = String(value.getHours()).padStart(2, '0');
  const minutes = String(value.getMinutes()).padStart(2, '0');
  return `${hours}:${minutes}`;
}

export function toDateOnly(value: string | null | undefined): Date | null {
  if (!value) {
    return null;
  }

  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
}

export function toTimeOnly(value: string | null | undefined): Date | null {
  if (!value) {
    return null;
  }

  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return null;
  }

  const time = new Date();
  time.setHours(parsed.getHours(), parsed.getMinutes(), 0, 0);
  return time;
}
