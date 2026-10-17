import { Component, input } from '@angular/core';
import { LucideDynamicIcon, LucideIcon, LucideSearchX } from '@lucide/angular';

@Component({
    selector: 'app-empty-state',
    imports: [LucideDynamicIcon],
    template: `
        <div class="flex flex-col items-center gap-4 rounded-card border border-dashed border-border bg-surface-raised/60 px-6 py-12 text-center">
            <span class="flex h-14 w-14 items-center justify-center rounded-full bg-accent-soft text-accent">
                <svg [lucideIcon]="icon()" class="h-6 w-6"></svg>
            </span>
            <div class="max-w-sm">
                <h3 class="font-brand text-lg font-semibold text-fg">{{ title() }}</h3>
                <p class="mt-1 text-sm text-fg-muted">{{ message() }}</p>
            </div>
            <ng-content />
        </div>
    `,
    host: { class: 'block' },
})
export class EmptyStateComponent {
    readonly icon = input<LucideIcon>(LucideSearchX);
    readonly title = input('Nothing here yet');
    readonly message = input('Try adjusting your search or filters.');
}
