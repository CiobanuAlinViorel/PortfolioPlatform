import { Component, input } from '@angular/core';

@Component({
    selector: 'app-timeline',
    template: `
        <ol class="relative flex flex-col gap-8 before:absolute before:bottom-2 before:left-[11px] before:top-2 before:w-px before:bg-gradient-to-b before:from-accent/50 before:via-border before:to-transparent md:gap-10 md:before:left-[calc(9rem+11px)]">
            <ng-content />
        </ol>
    `,
    host: { class: 'block' },
})
export class TimelineComponent {}

/**
 * Timeline entry. `rail` is the label shown on the rail (date range + duration);
 * `current` renders an accent dot with a pulsing ring.
 */
@Component({
    selector: 'app-timeline-item',
    template: `
        <li class="relative grid grid-cols-[1.5rem_1fr] gap-x-4 md:grid-cols-[9rem_1.5rem_1fr] md:gap-x-6">
            <div class="hidden pt-5 text-right md:block">
                @if (rail()) {
                    <p class="text-sm font-medium text-fg">{{ rail() }}</p>
                }
                @if (railSub()) {
                    <p class="mt-0.5 text-xs text-fg-muted">{{ railSub() }}</p>
                }
            </div>
            <div class="relative flex justify-center pt-5">
                <span class="relative flex h-[22px] w-[22px] items-center justify-center" aria-hidden="true">
                    @if (current()) {
                        <span class="absolute inset-1 rounded-full bg-accent/60 motion-safe:animate-pulse-ring"></span>
                    }
                    <span class="relative h-3 w-3 rounded-full ring-4 ring-surface" [class]="current() ? 'bg-accent' : 'bg-fg-subtle'"></span>
                </span>
            </div>
            <div class="min-w-0">
                @if (rail() || railSub()) {
                    <p class="mb-2 text-sm text-fg-muted md:hidden">
                        <span class="font-medium text-fg">{{ rail() }}</span>
                        @if (railSub()) { · {{ railSub() }} }
                    </p>
                }
                <ng-content />
            </div>
        </li>
    `,
    host: { class: 'contents' },
})
export class TimelineItemComponent {
    readonly rail = input('');
    readonly railSub = input('');
    readonly current = input(false);
}
