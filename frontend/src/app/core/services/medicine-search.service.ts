import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/common.models';
import { Medicine, MedicineSuggestion } from '../models/pharmacy.models';

@Injectable({ providedIn: 'root' })
export class MedicineSearchService {
  private apiUrl = `${environment.apiUrl}/medicines`;

  constructor(private http: HttpClient) {}

  getActive(): Observable<ApiResponse<Medicine[]>> {
    return this.http.get<ApiResponse<Medicine[]>>(`${this.apiUrl}/active`);
  }

  search(keyword: string): Observable<ApiResponse<MedicineSuggestion[]>> {
    return this.http.get<ApiResponse<MedicineSuggestion[]>>(`${this.apiUrl}/search`, {
      params: { q: keyword },
    });
  }

  reindex(): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(`${this.apiUrl}/reindex`, {});
  }
}
