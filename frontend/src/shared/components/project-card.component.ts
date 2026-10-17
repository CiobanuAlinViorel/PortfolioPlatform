import { Component, computed, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideArrowRight, LucideFolderKanban, LucideExternalLink } from '@lucide/angular';
import { BrandIconComponent } from './brand-icon.component';
import { BadgeComponent } from './badge.component';
import { EnumLabelPipe } from '../pipes/enum-label.pipe';
import { statusVariant } from '../utils/status-variant';

/** Structural shape shared by ProjectListItem, ProjectInJob and ProjectInVolunteer. */
export interface ProjectCardData {
    id: number;
    title: string;
    description?: string;
    status: string;
    complexity?: string;
    year?: number;
    tags?: string[];
    skills?: string[];
    imageUrl?: string;
    githubUrl?: string;
    demoUrl?: string;
}

@Component({
    selector: 'app-project-card',
    imports: [RouterLink, LucideArrowRight, LucideFolderKanban, LucideExternalLink, BadgeComponent, EnumLabelPipe, BrandIconComponent],
    template: `
        <a [routerLink]="['/projects', project().id]"
            class="group flex h-full flex-col overflow-hidden rounded-card border border-border bg-surface-raised shadow-elev-1 transition-[border-color,box-shadow,transform] duration-200 ease-out hover:border-accent/40 hover:shadow-elev-2 motion-safe:hover:-translate-y-0.5"
            [class.flex-row]="compact()" [class.items-stretch]="compact()">

            @if (!compact()) {
                <div class="relative aspect-video overflow-hidden bg-surface-muted">
                    @if (project().imageUrl && !imageFailed()) {
                        <img [src]="project().imageUrl" [alt]="project().title" loading="lazy" decoding="async" (error)="imageFailed.set(true)"
                            class="h-full w-full object-cover transition-transform duration-500 ease-out motion-safe:group-hover:scale-[1.04]" />
                        <div class="pointer-events-none absolute inset-x-0 bottom-0 h-1/2 bg-gradient-to-t from-black/35 to-transparent"></div>
                    } @else {
                        <div class="flex h-full w-full items-center justify-center text-fg-subtle">
                            <svg lucideFolderKanban class="h-10 w-10 opacity-60"></svg>
                        </div>
                    }
                    <div class="absolute left-3 top-3 flex gap-2">
                        <app-badge [variant]="variant()" [dot]="true">{{ project().status | enumLabel }}</app-badge>
                    </div>
                    @if (project().year) {
                        <span class="absolute right-3 top-3 rounded-full bg-surface-overlay/85 px-2.5 py-0.5 text-xs font-semibold text-fg shadow-elev-1 backdrop-blur">{{ project().year }}</span>
                    }
                </div>
            }

            <div class="flex flex-1 flex-col gap-3 p-5" [class.sm:p-6]="!compact()">
                <div class="min-w-0">
                    <div class="flex items-start justify-between gap-3">
                        <h3 class="line-clamp-2 text-lg font-semibold leading-snug text-fg transition-colors group-hover:text-accent">{{ project().title }}</h3>
                        @if (compact()) {
                            <app-badge [variant]="variant()" [dot]="true">{{ project().status | enumLabel }}</app-badge>
                        }
                    </div>
                    @if (compact() && project().year) {
                        <p class="mt-0.5 text-xs text-fg-subtle">{{ project().year }}</p>
                    }
                    @if (project().description) {
                        <p class="mt-2 line-clamp-2 text-sm leading-relaxed text-fg-muted">{{ project().description }}</p>
                    }
                </div>

                @if (chips().length) {
                    <div class="flex flex-wrap gap-1.5">
                        @for (chip of chips(); track chip) {
                            <span class="rounded-full border border-border bg-surface px-2.5 py-0.5 text-xs font-medium text-fg-muted">{{ chip }}</span>
                        }
                        @if (overflow() > 0) {
                            <span class="rounded-full bg-surface-muted px-2.5 py-0.5 text-xs font-medium text-fg-muted">+{{ overflow() }}</span>
                        }
                    </div>
                }

                <div class="mt-auto flex items-center justify-between gap-3 pt-2 text-sm">
                    <span class="flex items-center gap-3 text-fg-subtle">
                        @if (project().complexity) {
                            <span>{{ project().complexity | enumLabel }}</span>
                        }
                        @if (compact()) {
                            @if (project().githubUrl) {
                                <app-brand-icon name="github" class="h-4 w-4" aria-label="Has source code" />
                            }
                            @if (project().demoUrl) {
                                <svg lucideExternalLink class="h-4 w-4" aria-label="Has live demo"></svg>
                            }
                        }
                    </span>
                    <span class="inline-flex items-center gap-1 font-semibold text-accent">
                        View details
                        <svg lucideArrowRight class="h-4 w-4 transition-transform duration-200 motion-safe:group-hover:translate-x-0.5"></svg>
                    </span>
                </div>
            </div>
        </a>
    `,
    host: { class: 'block h-full' },
})
export class ProjectCardComponent {
    readonly project = input.required<ProjectCardData>();
    readonly compact = input(false);
    readonly maxChips = input(4);

    readonly imageFailed = signal(false);
    readonly variant = computed(() => statusVariant(this.project().status));
    private readonly allChips = computed(() => {
        const p = this.project();
        return (p.skills?.length ? p.skills : p.tags) ?? [];
    });
    readonly chips = computed(() => this.allChips().slice(0, this.maxChips()));
    readonly overflow = computed(() => Math.max(0, this.allChips().length - this.maxChips()));
}
