import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface Toast {
  id: number;
  message: string;
  kind: 'success' | 'error' | 'info';
}

export type ToastEvent = {
  type: 'add'
  toast: Toast
} | {
  type: 'remove'
  id: number
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private subject = new Subject<ToastEvent>();
  private counter = 0;
  private _toasts: Toast[] = [];

  public get events() { return this.subject.asObservable(); }

  showSuccess(message: string, duration: number = 3000) {
    this.showToast({ kind: 'success', message, id: this.counter++ }, duration);
  }

  showError(message: string, duration: number = 3000) {
    this.showToast({ kind: 'error', message, id: this.counter++ }, duration);
  }

  showInfo(message: string, duration: number = 3000) {
    this.showToast({ kind: 'info', message, id: this.counter++ }, duration);
  }

  private showToast(toast: Toast, duration: number) {
    this._toasts.push(toast);
    this.subject.next({ type: 'add', toast });

    if (this._toasts.length > 5) {
      this.clearToast(this._toasts[0].id);
    }

    setTimeout(() => {
      this.clearToast(toast.id);
    }, duration);
  }

  clearToast(id: number) {
    this._toasts = this._toasts.filter(toast => toast.id !== id);
    this.subject.next({ type: 'remove', id });
  }

  public get toasts(): ReadonlyArray<Toast> {
    return this._toasts;
  }
}
