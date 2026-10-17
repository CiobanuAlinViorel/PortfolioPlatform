import { Component, computed, input, model, output, signal } from '@angular/core';
import { LucideSlidersHorizontal, LucideX } from '@lucide/angular';
import { ChipComponent } from './chip.component';
import { DialogComponent } from './dialog.component';
import { ButtonComponent } from './button.component';

export interface FilterOption {
    id: string;
    label: string;
    count?: number;
}

export interface FilterGroup {
    id: string;
    label: string;
    options: FilterOption[];
}

export interface SortOption {
    id: string;
    label: string;
}

/** Single-select per group. `selected` maps group id -> option id (absent = all). */
export type FilterSelection = Record<string, string>;

/**
 * Inline filter chips on >= md, "Filters (n)" button + bottom sheet below.
 * Groups with < 2 options are hidden automatically.
 */
@Component({
    selector: 'app-filter-bar',
    imports: [LucideSlidersHorizontal, LucideX, ChipComponent, DialogComponent, ButtonComponent],
    template: `
        <div class="flex flex-col gap-3">
            <div class="flex flex-wrap items-center gap-3">
                <!-- Desktop groups -->
                @if (visibleGroups().length) {
                    <div class="hidden flex-1 flex-col gap-2.5 md:flex">
                        @for (group of visibleGroups(); track group.id) {
                            <div class="flex flex-wrap items-center gap-2">
                                <span class="mr-1 w-20 shrink-0 text-xs font-semibold uppercase tracking-wide text-fg-subtle">{{ group.label }}</span>
                                <app-chip [selected]="!selected()[group.id]" (toggled)="select(group.id, null)">All</app-chip>
                                @for (opt of group.options; track opt.id) {
                                    <app-chip [selected]="selected()[group.id] === opt.id" [count]="opt.count" (toggled)="select(group.id, opt.id)">
                                        {{ opt.label }}
                                    </app-chip>
                                }
                            </div>
                        }
                    </div>

                    <!-- Mobile trigger -->
                    <app-button variant="secondary" size="md" extraClass="md:hidden" (click)="sheetOpen.set(true)" [ariaLabel]="'Open filters'">
                        <svg lucideSlidersHorizontal class="h-4 w-4"></svg>
                        Filters
                        @if (activeCount()) {
                            <span class="ml-0.5 inline-flex h-5 min-w-5 items-center justify-center rounded-full bg-accent px-1.5 text-xs font-semibold text-accent-fg">{{ activeCount() }}</span>
                        }
                    </app-button>
                }

                <!-- Sort -->
                @if (sortOptions().length) {
                    <label class="ml-auto flex items-center gap-2 text-sm text-fg-muted">
                        <span class="hidden sm:inline">Sort</span>
                        <select [value]="sort()" (change)="onSort($event)" aria-label="Sort"
                            class="h-10 rounded-control border border-border bg-surface-raised px-3 text-sm font-medium text-fg shadow-elev-1 outline-none transition-colors focus:border-accent focus:ring-2 focus:ring-accent/30">
                            @for (opt of sortOptions(); track opt.id) {
                                <option [value]="opt.id" [selected]="opt.id === sort()">{{ opt.label }}</option>
                            }
                        </select>
                    </label>
                }
            </div>

            <!-- Result count + active filters -->
            <div class="flex flex-wrap items-center gap-2 text-sm text-fg-muted">
                @if (resultLabel()) {
                    <span aria-live="polite" class="tabular-nums">{{ resultLabel() }}</span>
                }
                @for (chip of activeChips(); track chip.groupId) {
                    <app-chip [selected]="true" [removable]="true" (toggled)="select(chip.groupId, null)">{{ chip.label }}</app-chip>
                }
                @if (activeCount()) {
                    <button type="button" (click)="clearAll()" class="inline-flex items-center gap-1 text-sm font-medium text-accent hover:underline">
                        <svg lucideX class="h-3.5 w-3.5"></svg>
                        Clear all
                    </button>
                }
            </div>
        </div>

        <app-dialog [(open)]="sheetOpen" mode="sheet" title="Filters">
            <div class="flex flex-col gap-6 pb-2">
                @for (group of visibleGroups(); track group.id) {
                    <fieldset>
                        <legend class="mb-3 text-xs font-semibold uppercase tracking-wide text-fg-subtle">{{ group.label }}</legend>
                        <div class="flex flex-wrap gap-2">
                            <app-chip [selected]="!selected()[group.id]" (toggled)="select(group.id, null)">All</app-chip>
                            @for (opt of group.options; track opt.id) {
                                <app-chip [selected]="selected()[group.id] === opt.id" [count]="opt.count" (toggled)="select(group.id, opt.id)">
                                    {{ opt.label }}
                                </app-chip>
                            }
                        </div>
                    </fieldset>
                }
            </div>
            <div footer class="flex gap-2 border-t border-border px-5 py-4">
                <app-button variant="secondary" [block]="true" extraClass="flex-1" (click)="clearAll()">Clear</app-button>
                <app-button variant="primary" [block]="true" extraClass="flex-1" (click)="sheetOpen.set(false)">Show {{ resultCount() ?? '' }} results</app-button>
            </div>
        </app-dialog>
    `,
    host: { class: 'block' },
})
export class FilterBarComponent {
    readonly groups = input<FilterGroup[]>([]);
    readonly selected = model<FilterSelection>({});
    readonly sortOptions = input<SortOption[]>([]);
    readonly sort = model<string>('');
    readonly resultCount = input<number | null>(null);
    readonly totalCount = input<number | null>(null);
    readonly noun = input('results');

    readonly cleared = output<void>();
    readonly sheetOpen = signal(false);

    readonly visibleGroups = computed(() => this.groups().filter((g) => g.options.length >= 2));
    readonly activeCount = computed(() => Object.values(this.selected()).filter(Boolean).length);

    readonly activeChips = computed(() =>
        Object.entries(this.selected())
            .filter(([, v]) => !!v)
            .map(([groupId, optId]) => {
                const group = this.groups().find((g) => g.id === groupId);
                const opt = group?.options.find((o) => o.id === optId);
                return { groupId, label: `${group?.label ?? groupId}: ${opt?.label ?? optId}` };
            }),
    );

    readonly resultLabel = computed(() => {
        const count = this.resultCount();
        const total = this.totalCount();
        if (count === null) return '';
        if (total !== null && total !== count) return `${count} of ${total} ${this.noun()}`;
        return `${count} ${this.noun()}`;
    });

    select(groupId: string, optionId: string | null): void {
        const next = { ...this.selected() };
        if (optionId === null) delete next[groupId];
        else next[groupId] = optionId;
        this.selected.set(next);
    }

    clearAll(): void {
        this.selected.set({});
        this.cleared.emit();
    }

    onSort(event: Event): void {
        this.sort.set((event.target as HTMLSelectElement).value);
    }
}
