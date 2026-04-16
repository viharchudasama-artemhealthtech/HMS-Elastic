export enum MedicineCategory {
  ANALGESICS = 'ANALGESICS',
  ANTIBIOTICS = 'ANTIBIOTICS',
  ANTISEPTICS = 'ANTISEPTICS',
  VITAMINS = 'VITAMINS',
  CARDIAC = 'CARDIAC',
  DIABETIC = 'DIABETIC',
  OTHER = 'OTHER',
}

export interface Medicine {
  id: number;
  name: string;
  medicineCode: string;
  category: MedicineCategory | string;
  manufacturer: string;
  description?: string;
  unitPrice: number;
  quantityInStock: number;
  reorderLevel: number;
  expiryDate?: string;
  isActive: boolean;
  createdAt?: string;
}

export interface MedicineRequest {
  name: string;
  medicineCode: string;
  category: MedicineCategory | string;
  manufacturer: string;
  description?: string;
  unitPrice: number;
  quantityInStock: number;
  reorderLevel: number;
  expiryDate?: string;
}

export interface DispenseMedicineRequest {
  prescriptionId: number;
  items: DispenseItem[];
}

export interface DispenseItem {
  medicineId: number;
  quantity: number;
}

export interface InventoryTransaction {
  id: number;
  medicineId: number;
  medicineName: string;
  medicineCode: string;
  transactionType: 'IN' | 'OUT';
  quantity: number;
  referenceId?: number;
  notes?: string;
  createdAt: string;
  createdBy: string;
}

export interface MedicineSuggestion {
  id: number;
  name: string;
  brand: string;
  stock: number;
  inStock: boolean;
}

export interface MedicineSlice {
  content: Medicine[];
  page: number;
  size: number;
  hasNext: boolean;
  hasPrevious: boolean;
  total?: number;
  inStockCount?: number;
  outOfStockCount?: number;
}
