export type Role = 'CLIENTE' | 'MESERO' | 'COCINERO' | 'ADMIN' | 'SUPER_ADMIN';
export type AuthProvider = 'LOCAL' | 'GOOGLE' | 'APPLE';

/** Respuesta de /api/auth/login, /register, /refresh */
export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
}

export interface OAuthRequest {
  idToken: string;
}

/** Respuesta de GET /api/auth/me */
export interface AuthUser {
  id: string;
  email: string;
  fullName: string;
  role: Role;
  provider: AuthProvider;
  enabled: boolean;
  createdAt: string;
}
