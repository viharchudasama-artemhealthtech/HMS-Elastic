import { HttpErrorResponse } from '@angular/common/http';
import { ErrorHandler, Injectable, Injector } from '@angular/core';
import { StatusModalService } from '../services/status-modal.service';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  private handlingError = false;

  constructor(private injector: Injector) {}

  handleError(error: unknown): void {
    // Prevent infinite loops if modal rendering itself throws.
    if (this.handlingError) {
      console.error('Recursive global error detected:', error);
      return;
    }

    this.handlingError = true;
    try {
      const statusModalService = this.injector.get(StatusModalService);
      const message = this.resolveMessage(error);

      console.error('Global runtime error:', error);
      statusModalService.showError('Unexpected Error', message);
    } finally {
      this.handlingError = false;
    }
  }

  private resolveMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      return error.error?.message || error.message || 'Server request failed. Please try again.';
    }

    if (error instanceof Error) {
      return error.message || 'A runtime error occurred.';
    }

    if (typeof error === 'string' && error.trim().length > 0) {
      return error;
    }

    return 'A runtime error occurred. Please try again.';
  }
}
