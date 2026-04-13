import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/common.models';
import { InventoryTransaction } from '../models/pharmacy.models';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private inventoryUrl = `${environment.apiUrl}/pharmacy/inventory-log`;
  private medicinesUrl = `${environment.apiUrl}/medicines`;

  constructor(private http: HttpClient) {}

  getInventoryLog(): Observable<ApiResponse<InventoryTransaction[]>> {
    return this.http.get<ApiResponse<InventoryTransaction[]>>(this.inventoryUrl);
  }

  restock(id: number, quantity: number): Observable<ApiResponse<void>> {
    return this.http.patch<ApiResponse<void>>(`${this.medicinesUrl}/${id}/restock`, { quantity });
  }
}
