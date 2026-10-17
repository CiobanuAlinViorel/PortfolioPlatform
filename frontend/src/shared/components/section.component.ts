import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideArrowRight } from '@lucide/angular';
import { RevealDirective } from '../directives/reveal.directive';

/** Home-page section wrapper: eyebrow, title, description, "View all" link. */
@Component({
    selector: 'app-section',
    imports: [RouterLink, LucideArrowRight, RevealDirective],
    template: `
        <section class="py-14 sm:py-20" [attr.aria-labelledby]="id()">
            <div class="mx-auto max-w-6xl px-4 sm:px-6 lg:px-8">
                <div appReveal class="mb-8 flex flex-col gap-4 sm:mb-10 sm:flex-row sm:items-end sm:justify-between">
                    <div class="max-w-2xl">
                        @if (eyebrow()) {
                            <p class="mb-2 text-xs font-semibold uppercase tracking-[0.18em] text-accent">{{ eyebrow() }}</p>
                        }
                        <h2 [id]="id()" class="text-title font-semibold text-fg">{{ title() }}</h2>
                        @if (description()) {
                            <p class="mt-3 text-base text-fg-muted sm:text-lg">{{ description() }}</p>
                        }
                    </div>
                    @if (viewAllLink()) {
                        <a [routerLink]="viewAllLink()"
                            class="group inline-flex shrink-0 items-center gap-1.5 text-sm font-semibold text-accent transition-colors hover:text-accent-hover">
                            {{ viewAllLabel() }}
                            <svg lucideArrowRight class="h-4 w-4 transition-transform duration-200 motion-safe:group-hover:translate-x-0.5"></svg>
                        </a>
                    }
                </div>
                <ng-content />
            </div>
        </section>
    `,
    host: { class: 'block' },
})
export class SectionComponent {
    readonly id = input.required<string>();
    readonly title = input.required<string>();
    readonly eyebrow = input('');
    readonly description = input('');
    readonly viewAllLink = input<string | null>(null);
    readonly viewAllLabel = input('View all');
}
