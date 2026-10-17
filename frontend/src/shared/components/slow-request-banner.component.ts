import { Component, inject } from '@angular/core';
import { LucideLoaderCircle, LucideX } from '@lucide/angular';
import { SlowRequestState } from '../services/slow-request.interceptor';

@Component({
    selector: 'app-slow-request-banner',
    imports: [LucideLoaderCircle, LucideX],
    template: `
        @if (state.isSlow() && !state.dismissed()) {
            <div class="pointer-events-none fixed inset-x-0 top-[calc(var(--header-h)+0.75rem)] z-40 flex justify-center px-4" role="status" aria-live="polite">
                <div class="pointer-events-auto flex items-center gap-3 rounded-full border border-border bg-surface-overlay/90 py-2 pl-3 pr-2 text-sm text-fg shadow-elev-3 backdrop-blur-md motion-safe:animate-fade-up">
                    <svg lucideLoaderCircle class="h-4 w-4 shrink-0 animate-spin text-accent"></svg>
                    <span>The server is waking up — this can take up to 20 seconds on first visit.</span>
                    <button type="button" (click)="state.dismiss()" aria-label="Dismiss"
                        class="flex h-7 w-7 shrink-0 items-center justify-center rounded-full text-fg-muted transition-colors hover:bg-surface-muted hover:text-fg">
                        <svg lucideX class="h-3.5 w-3.5"></svg>
                    </button>
                </div>
            </div>
        }
    `,
})
export class SlowRequestBannerComponent {
    readonly state = inject(SlowRequestState);
}
