/** Página de Spring Data (endpoints paginados). */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first?: boolean;
  last?: boolean;
}

/** Respuesta de error RFC 7807 (ProblemDetail) que devuelve el backend. */
export interface ProblemDetail {
  type?: string;
  title?: string;
  status: number;
  detail?: string;
  timestamp?: string;
  errors?: Record<string, string>;
  path?: string;
}
