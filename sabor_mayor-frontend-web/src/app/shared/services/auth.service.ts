import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService } from './api.service';
import { TokenStorageService } from './token-storage.service';
import {
  AuthUser,
  LoginRequest,
  OAuthRequest,
  RegisterRequest,
  Role,
  TokenResponse,
} from '../models/auth.model';

/**
 * Estado de autenticación centralizado con signals.
 * Maneja login/registro/OAuth, rotación de refresh tokens y carga del perfil.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(ApiService);
  private readonly tokens = inject(TokenStorageService);

  readonly currentUser = signal<AuthUser | null>(null);
  readonly isAuthenticated = computed(() => this.currentUser() !== null);
  readonly role = computed<Role | null>(() => this.currentUser()?.role ?? null);

  /** ¿Hay un token persistido? (antes de resolver el perfil). */
  hasToken(): boolean {
    return this.tokens.hasSession();
  }

  login(body: LoginRequest): Observable<AuthUser> {
    return this.exchange('/api/auth/login', body);
  }

  register(body: RegisterRequest): Observable<AuthUser> {
    return this.exchange('/api/auth/register', body);
  }

  oauth(provider: 'google' | 'apple', body: OAuthRequest): Observable<AuthUser> {
    return this.exchange(`/api/auth/oauth2/${provider}`, body);
  }

  /** Carga el perfil del usuario autenticado (GET /api/auth/me). */
  loadCurrentUser(): Observable<AuthUser> {
    return this.api.get<AuthUser>('/api/auth/me').pipe(tap((user) => this.currentUser.set(user)));
  }

  /** Rotación de refresh token. Usado por el interceptor ante un 401. */
  refresh(): Observable<TokenResponse> {
    const refreshToken = this.tokens.refreshToken;
    return this.api
      .post<TokenResponse>('/api/auth/refresh', { refreshToken })
      .pipe(tap((res) => this.tokens.setTokens(res.accessToken, res.refreshToken)));
  }

  logout(): void {
    // Best-effort: revoca en el backend; limpia local pase lo que pase.
    this.api.post('/api/auth/logout').subscribe({ next: () => {}, error: () => {} });
    this.tokens.clear();
    this.currentUser.set(null);
  }

  /** Limpieza local sin llamar al backend (ej. refresh falló). */
  clearSession(): void {
    this.tokens.clear();
    this.currentUser.set(null);
  }

  private exchange(path: string, body: unknown): Observable<AuthUser> {
    return new Observable<AuthUser>((subscriber) => {
      const sub = this.api.post<TokenResponse>(path, body).subscribe({
        next: (res) => {
          this.tokens.setTokens(res.accessToken, res.refreshToken);
          this.loadCurrentUser().subscribe({
            next: (user) => {
              subscriber.next(user);
              subscriber.complete();
            },
            error: (err) => subscriber.error(err),
          });
        },
        error: (err) => subscriber.error(err),
      });
      return () => sub.unsubscribe();
    });
  }
}
