import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideChevronRight } from '@lucide/angular';

export interface BreadcrumbItem {
    label: string;
    link?: string | unknown[];
}

@Component({
    selector: 'app-breadcrumb',
    imports: [RouterLink, LucideChevronRight],
    template: `
        <nav aria-label="Breadcrumb" class="text-sm text-fg-muted">
            <ol class="flex flex-wrap items-center gap-1.5">
                @for (item of items(); track $index; let last = $last) {
                    <li class="flex items-center gap-1.5">
                        @if (item.link && !last) {
                            <a [routerLink]="item.link" class="rounded-sm transition-colors hover:text-fg">{{ item.label }}</a>
                        } @else {
                            <span class="truncate font-medium text-fg" [attr.aria-current]="last ? 'page' : null">{{ item.label }}</span>
                        }
                        @if (!last) {
                            <svg lucideChevronRight class="h-3.5 w-3.5 text-fg-subtle" aria-hidden="true"></svg>
                        }
                    </li>
                }
            </ol>
        </nav>
    `,
    host: { class: 'block' },
})
export class BreadcrumbComponent {
    readonly items = input.required<BreadcrumbItem[]>();
}
