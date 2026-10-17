import { Component, input, output } from '@angular/core';
import { LucideCheck, LucideX } from '@lucide/angular';

/** Toggle chip used by filter bars and quick filters. */
@Component({
    selector: 'app-chip',
    imports: [LucideCheck, LucideX],
    template: `
        <button type="button" (click)="toggled.emit()" [attr.aria-pressed]="selected()" [disabled]="disabled()"
            class="inline-flex h-9 items-center gap-1.5 whitespace-nowrap rounded-full border px-3.5 text-sm font-medium transition-[color,background-color,border-color,transform] duration-200 ease-out active:scale-[0.97] disabled:opacity-50"
            [class]="selected()
                ? 'border-accent bg-accent-soft text-accent'
                : 'border-border bg-surface-raised text-fg-muted hover:border-accent/40 hover:text-fg'">
            @if (selected() && !removable()) {
                <svg lucideCheck class="h-3.5 w-3.5"></svg>
            }
            <ng-content />
            @if (count() !== null && count() !== undefined) {
                <span class="text-xs tabular-nums opacity-70">{{ count() }}</span>
            }
            @if (removable()) {
                <svg lucideX class="h-3.5 w-3.5"></svg>
            }
        </button>
    `,
    host: { class: 'inline-flex' },
})
export class ChipComponent {
    readonly selected = input(false);
    readonly disabled = input(false);
    readonly removable = input(false);
    readonly count = input<number | null | undefined>(null);
    readonly toggled = output<void>();
}
