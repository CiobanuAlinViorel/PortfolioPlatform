import { Component, computed, input } from '@angular/core';
import { BadgeVariant } from '../utils/status-variant';

const VARIANTS: Record<BadgeVariant, string> = {
    neutral: 'bg-surface-muted text-fg-muted',
    success: 'bg-success-soft text-success',
    info: 'bg-info-soft text-info',
    warning: 'bg-warning-soft text-warning',
    danger: 'bg-danger-soft text-danger',
    accent: 'bg-accent-soft text-accent',
};

@Component({
    selector: 'app-badge',
    template: `
        @if (dot()) {
            <span class="h-1.5 w-1.5 shrink-0 rounded-full bg-current" aria-hidden="true"></span>
        }
        <ng-content />
    `,
    host: { '[class]': 'classes()' },
})
export class BadgeComponent {
    readonly variant = input<BadgeVariant>('neutral');
    readonly size = input<'sm' | 'md'>('sm');
    readonly dot = input(false);

    readonly classes = computed(() =>
        [
            'inline-flex items-center gap-1.5 whitespace-nowrap rounded-full font-medium',
            this.size() === 'sm' ? 'px-2.5 py-0.5 text-xs' : 'px-3 py-1 text-sm',
            VARIANTS[this.variant()],
        ].join(' '),
    );
}
