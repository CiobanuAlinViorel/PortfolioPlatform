import { Component, input, output } from '@angular/core';
import { LucideCircleAlert, LucideRotateCcw } from '@lucide/angular';
import { ButtonComponent } from './button.component';

@Component({
    selector: 'app-error-state',
    imports: [LucideCircleAlert, LucideRotateCcw, ButtonComponent],
    template: `
        <div class="flex flex-col items-center gap-4 rounded-card border border-danger/20 bg-surface-raised px-6 py-12 text-center" role="alert">
            <span class="flex h-14 w-14 items-center justify-center rounded-full bg-danger-soft text-danger">
                <svg lucideCircleAlert class="h-6 w-6"></svg>
            </span>
            <div class="max-w-sm">
                <h3 class="font-brand text-lg font-semibold text-fg">{{ title() }}</h3>
                <p class="mt-1 text-sm text-fg-muted">{{ message() }}</p>
            </div>
            <div class="flex flex-wrap justify-center gap-2">
                @if (showRetry()) {
                    <app-button variant="primary" size="sm" (click)="retry.emit()">
                        <svg lucideRotateCcw class="h-4 w-4"></svg>
                        Try again
                    </app-button>
                }
                <ng-content />
            </div>
        </div>
    `,
    host: { class: 'block' },
})
export class ErrorStateComponent {
    readonly title = input('Something went wrong');
    readonly message = input("We couldn't load this content. Check your connection and try again.");
    readonly showRetry = input(true);
    readonly retry = output<void>();
}
