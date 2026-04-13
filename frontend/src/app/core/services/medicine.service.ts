import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/common.models';
import { Medicine, MedicineRequest, MedicineSlice } from '../models/pharmacy.models';

@Injectable({ providedIn: 'root' })
export class MedicineService {
  private apiUrl = `${environment.apiUrl}/medicines`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ApiResponse<Medicine[]>> {
    return this.http.get<ApiResponse<Medicine[]>>(this.apiUrl);
  }

  getPaged(page: number, size: number): Observable<ApiResponse<MedicineSlice>> {
    return this.http.get<ApiResponse<MedicineSlice>>(`${this.apiUrl}/paged`, {
      params: { page, size },
    });
  }

  getById(id: number): Observable<ApiResponse<Medicine>> {
    return this.http.get<ApiResponse<Medicine>>(`${this.apiUrl}/${id}`);
  }

  create(medicine: MedicineRequest): Observable<ApiResponse<Medicine>> {
    return this.http.post<ApiResponse<Medicine>>(this.apiUrl, medicine);
  }

  update(id: number, medicine: MedicineRequest): Observable<ApiResponse<Medicine>> {
    return this.http.put<ApiResponse<Medicine>>(`${this.apiUrl}/${id}`, medicine);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
