import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, from, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

const AUTH_REFRESH_URL_SEGMENT = '/auth/refresh';
const AUTH_LOGIN_URL_SEGMENT = '/auth/login';
const AUTH_RETRY_HEADER = 'X-Auth-Retry';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const accessToken = authService.getAccessToken();
  const isRefreshRequest = req.url.includes(AUTH_REFRESH_URL_SEGMENT);
  const isLoginRequest = req.url.includes(AUTH_LOGIN_URL_SEGMENT);
  const hasRetried = req.headers.has(AUTH_RETRY_HEADER);

  const authReq = req.clone({
    withCredentials: true,
    ...(accessToken ? { setHeaders: { Authorization: `Bearer ${accessToken}` } } : {}),
  });

  // switchmap is RsJX observable whenever new values comes in and cancels the previous one 
  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isRefreshRequest && !isLoginRequest && !hasRetried) {
        return from(authService.initializeSession()).pipe(
          switchMap(() => {
            const refreshedToken = authService.getAccessToken();

            if (!refreshedToken) {
              authService.logout(false).subscribe();
              return throwError(() => error);
            }

            const retriedRequest = req.clone({
              withCredentials: true,
              setHeaders: {
                Authorization: `Bearer ${refreshedToken}`,
                [AUTH_RETRY_HEADER]: 'true', // Mark the request as a retry to prevent infinite loops
              },
            });

            return next(retriedRequest);
          }),
          catchError((refreshError) => {
            authService.logout(false).subscribe();
            return throwError(() => refreshError);
          }),
        );
      }
      return throwError(() => error);
    }),
  );
};
