import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const isAuthRequest =
    req.url.includes('/auth/login') || req.url.includes('/auth/refresh') || req.url.includes('/auth/logout');

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !isAuthRequest) {
        authService.logout(false).subscribe({
          complete: () => {
            router.navigate(['/login']);
          },
        });
        return throwError(() => 'Session expired. Please log in again.');
      }

      let errorMessage = 'An unknown error occurred';

      if (err.error instanceof ErrorEvent) {
        errorMessage = `Network error: ${err.error.message}`;
        console.error('Network error:', err.error);
      } else {
        switch (err.status) {
          case 0:
            errorMessage = 'Unable to connect to server. Please check your internet connection.';
            break;
          case 401:
            errorMessage = 'Please log in again.';
            break;
          case 403:
            router.navigate(['/unauthorized']);
            errorMessage = 'You do not have permission to perform this action.';
            break;
          case 404:
            errorMessage = err.error?.message || 'Resource not found.';
            break;
          case 422:
            errorMessage = err.error?.message || 'Please check your input.';
            break;
          case 500:
            errorMessage = 'Internal server error. Please try again later.';
            break;
          default:
            errorMessage = err.error?.message || err.statusText || `HTTP error ${err.status}`;
        }
      }

      if (err.status === 400 && Array.isArray(err.error?.validationErrors) && err.error.validationErrors.length > 0) {
        const firstValidationError = err.error.validationErrors[0];
        const validationMessage = firstValidationError?.message || 'Validation failed.';
        return throwError(() => validationMessage);
      }

      return throwError(() => err.error?.message || errorMessage);
    }),
  );
};
