import { Component, input } from '@angular/core';

/**
 * Page header for list pages. Project `[actions]` for right-side controls and
 * `[toolbar]` for a full-width row (search/filters) under the title.
 */
@Component({
    selector: 'app-page-header',
    template: `
        <header class="mb-8 flex flex-col gap-6 sm:mb-10">
            <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
                <div class="max-w-2xl">
                    @if (eyebrow()) {
                        <p class="mb-2 text-xs font-semibold uppercase tracking-[0.18em] text-accent">{{ eyebrow() }}</p>
                    }
                    <h1 class="text-title font-semibold text-fg">{{ title() }}</h1>
                    @if (subtitle()) {
                        <p class="mt-3 text-base leading-relaxed text-fg-muted sm:text-lg">{{ subtitle() }}</p>
                    }
                </div>
                <div class="flex shrink-0 flex-wrap gap-3">
                    <ng-content select="[actions]" />
                </div>
            </div>
            <ng-content select="[toolbar]" />
        </header>
    `,
    host: { class: 'block' },
})
export class PageHeaderComponent {
    readonly title = input.required<string>();
    readonly subtitle = input<string>('');
    readonly eyebrow = input<string>('');
}
