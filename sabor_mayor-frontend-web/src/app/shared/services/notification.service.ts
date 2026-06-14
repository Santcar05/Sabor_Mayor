import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'info' | 'warning';

export interface Toast {
  id: number;
  type: ToastType;
  title?: string;
  message: string;
  duration: number;
}

/**
 * Servicio de notificaciones (toasts) basado en signals.
 * El <app-toast-container> lee la señal `toasts` y los renderiza.
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private seq = 0;
  readonly toasts = signal<Toast[]>([]);

  success(message: string, title?: string, duration = 4000): void {
    this.push('success', message, title, duration);
  }
  error(message: string, title = 'Algo salió mal', duration = 6000): void {
    this.push('error', message, title, duration);
  }
  info(message: string, title?: string, duration = 4000): void {
    this.push('info', message, title, duration);
  }
  warning(message: string, title?: string, duration = 5000): void {
    this.push('warning', message, title, duration);
  }

  dismiss(id: number): void {
    this.toasts.update((list) => list.filter((t) => t.id !== id));
  }

  private push(type: ToastType, message: string, title: string | undefined, duration: number): void {
    const toast: Toast = { id: ++this.seq, type, title, message, duration };
    this.toasts.update((list) => [...list, toast]);
    if (duration > 0) {
      setTimeout(() => this.dismiss(toast.id), duration);
    }
  }
}
