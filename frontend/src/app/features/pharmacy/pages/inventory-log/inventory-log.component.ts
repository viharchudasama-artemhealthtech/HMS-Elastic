import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { ApiResponse } from '../../../../core/models/common.models';
import { InventoryTransaction } from '../../../../core/models/pharmacy.models';
import { PharmacyService } from '../../../../core/services/pharmacy.service';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';

@Component({
  selector: 'app-inventory-log',
  standalone: true,
  imports: [CommonModule, TableModule, InputTextModule, SidebarComponent, HeaderComponent],
  templateUrl: './inventory-log.component.html',
  styleUrl: './inventory-log.component.scss',
})
export class InventoryLogComponent implements OnInit {
  
  transactions: InventoryTransaction[] = [];
  filteredTransactions: InventoryTransaction[] = [];
  isLoading = true;

  constructor(private pharmacyService: PharmacyService) {}

  ngOnInit(): void {
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.isLoading = true;
    this.pharmacyService.getInventoryLog().subscribe({
      next: (res: ApiResponse<InventoryTransaction[]>) => {
        this.transactions = res.data;
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

 // Placeholder for filter logic - currently just copies all transactions to filteredTransactions
  applyFilter(): void {
    this.filteredTransactions = this.transactions;
  }
}
