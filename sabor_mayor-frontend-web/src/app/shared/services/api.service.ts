import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

type ParamValue = string | number | boolean | null | undefined;
type ParamMap = Record<string, ParamValue | ParamValue[]>;

/**
 * Wrapper HTTP de bajo nivel sobre el API Gateway. Centraliza la base URL y la
 * construcción de query params. Los servicios de dominio dependen de éste.
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  readonly baseUrl = environment.apiUrl;

  get<T>(path: string, params?: ParamMap): Observable<T> {
    return this.http.get<T>(this.url(path), { params: this.buildParams(params) });
  }

  post<T>(path: string, body?: unknown, params?: ParamMap): Observable<T> {
    return this.http.post<T>(this.url(path), body ?? {}, { params: this.buildParams(params) });
  }

  put<T>(path: string, body?: unknown): Observable<T> {
    return this.http.put<T>(this.url(path), body ?? {});
  }

  patch<T>(path: string, body?: unknown, params?: ParamMap): Observable<T> {
    return this.http.patch<T>(this.url(path), body ?? {}, { params: this.buildParams(params) });
  }

  delete<T>(path: string): Observable<T> {
    return this.http.delete<T>(this.url(path));
  }

  /** Descarga binaria (ej. export.xlsx, factura PDF). */
  getBlob(path: string, params?: ParamMap): Observable<Blob> {
    return this.http.get(this.url(path), {
      params: this.buildParams(params),
      responseType: 'blob',
    });
  }

  private url(path: string): string {
    return `${this.baseUrl}${path.startsWith('/') ? path : `/${path}`}`;
  }

  private buildParams(params?: ParamMap): HttpParams {
    let httpParams = new HttpParams();
    if (!params) return httpParams;
    for (const [key, value] of Object.entries(params)) {
      if (value === null || value === undefined) continue;
      if (Array.isArray(value)) {
        for (const v of value) {
          if (v !== null && v !== undefined) httpParams = httpParams.append(key, String(v));
        }
      } else {
        httpParams = httpParams.set(key, String(value));
      }
    }
    return httpParams;
  }
}
