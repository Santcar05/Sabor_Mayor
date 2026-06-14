import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

const ACCESS_KEY = 'sm_access_token';
const REFRESH_KEY = 'sm_refresh_token';

/**
 * Almacenamiento de tokens seguro para SSR (no toca localStorage en el servidor).
 * El access token se mantiene también en memoria para minimizar lecturas.
 */
@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);
  private accessTokenMem: string | null = null;

  get accessToken(): string | null {
    if (this.accessTokenMem) return this.accessTokenMem;
    if (!this.isBrowser) return null;
    this.accessTokenMem = localStorage.getItem(ACCESS_KEY);
    return this.accessTokenMem;
  }

  get refreshToken(): string | null {
    if (!this.isBrowser) return null;
    return localStorage.getItem(REFRESH_KEY);
  }

  setTokens(accessToken: string, refreshToken: string): void {
    this.accessTokenMem = accessToken;
    if (this.isBrowser) {
      localStorage.setItem(ACCESS_KEY, accessToken);
      localStorage.setItem(REFRESH_KEY, refreshToken);
    }
  }

  clear(): void {
    this.accessTokenMem = null;
    if (this.isBrowser) {
      localStorage.removeItem(ACCESS_KEY);
      localStorage.removeItem(REFRESH_KEY);
    }
  }

  hasSession(): boolean {
    return !!this.accessToken;
  }
}
