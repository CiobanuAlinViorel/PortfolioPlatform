import { Component, inject } from '@angular/core';
import { LucideCheck, LucideCircleAlert, LucideX } from '@lucide/angular';
import { ToastService } from '../services/toast.service';

@Component({
    selector: 'app-toast-host',
    imports: [LucideCheck, LucideCircleAlert, LucideX],
    template: `
        <div class="pointer-events-none fixed inset-x-0 bottom-[calc(1rem+env(safe-area-inset-bottom))] z-[90] flex flex-col items-center gap-2 px-4"
            aria-live="polite" aria-atomic="true">
            @for (toast of toasts.toasts(); track toast.id) {
                <div class="pointer-events-auto flex items-center gap-2.5 rounded-full border border-border bg-surface-overlay py-2 pl-3 pr-2 text-sm font-medium text-fg shadow-elev-3 motion-safe:animate-fade-up">
                    @if (toast.variant === 'success') {
                        <span class="flex h-6 w-6 items-center justify-center rounded-full bg-success-soft text-success">
                            <svg lucideCheck class="h-3.5 w-3.5"></svg>
                        </span>
                    } @else if (toast.variant === 'danger') {
                        <span class="flex h-6 w-6 items-center justify-center rounded-full bg-danger-soft text-danger">
                            <svg lucideCircleAlert class="h-3.5 w-3.5"></svg>
                        </span>
                    }
                    {{ toast.message }}
                    <button type="button" (click)="toasts.dismiss(toast.id)" aria-label="Dismiss"
                        class="ml-1 flex h-7 w-7 items-center justify-center rounded-full text-fg-muted transition-colors hover:bg-surface-muted hover:text-fg">
                        <svg lucideX class="h-3.5 w-3.5"></svg>
                    </button>
                </div>
            }
        </div>
    `,
})
export class ToastHostComponent {
    readonly toasts = inject(ToastService);
}
