import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { $ } from 'protractor';
import { catchError } from 'rxjs/operators';
import { error } from 'console';
import { throwError } from 'rxjs';

@Injectable()
export class TokenInterceptorInterceptor implements HttpInterceptor {
  constructor(private router: Router) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    const token = localStorage.getItem('token');
    if (token) {
      request = request.clone({
        setHeaders: { 'Content-Type': 'application/json; charset=utf-8',
        'Accept': 'application/json',
        'Authorization': `Bearer ${token}` },
      });
    }
    return next.handle(request).pipe(
      catchError((err) => {
        if (err instanceof HttpErrorResponse) {
          console.log(err.url);
          if (err.status === 401 || err.status === 403) {
            if (this.router.url === '/') {
            } else {
              localStorage.clear();
              this.router.navigate(['/']);
            }
          }
        }
        return throwError(err);
      })
    );
  }
}
