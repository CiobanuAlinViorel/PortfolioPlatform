import { Injectable, signal } from '@angular/core';

export interface Toast {
    id: number;
    message: string;
    variant: 'neutral' | 'success' | 'danger';
}

@Injectable({ providedIn: 'root' })
export class ToastService {
    private nextId = 0;
    private readonly toastsSignal = signal<Toast[]>([]);
    readonly toasts = this.toastsSignal.asReadonly();

    show(message: string, variant: Toast['variant'] = 'neutral', durationMs = 2800): void {
        const id = ++this.nextId;
        this.toastsSignal.update((list) => [...list, { id, message, variant }]);
        setTimeout(() => this.dismiss(id), durationMs);
    }

    dismiss(id: number): void {
        this.toastsSignal.update((list) => list.filter((t) => t.id !== id));
    }
}
