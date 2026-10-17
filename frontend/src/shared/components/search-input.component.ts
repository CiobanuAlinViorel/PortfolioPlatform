import { Component, DestroyRef, inject, input, model } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { LucideSearch, LucideX } from '@lucide/angular';
import { Subject, debounceTime } from 'rxjs';

@Component({
    selector: 'app-search-input',
    imports: [LucideSearch, LucideX],
    template: `
        <label class="relative block">
            <span class="sr-only">{{ placeholder() }}</span>
            <svg lucideSearch class="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-fg-subtle"></svg>
            <input type="search" [value]="value()" (input)="onInput($event)" [placeholder]="placeholder()" autocomplete="off"
                class="h-11 w-full rounded-control border border-border bg-surface-raised pl-10 pr-10 text-sm text-fg shadow-elev-1 outline-none transition-[border-color,box-shadow] duration-200 placeholder:text-fg-subtle focus:border-accent focus:ring-2 focus:ring-accent/30 [&::-webkit-search-cancel-button]:hidden" />
            @if (value()) {
                <button type="button" (click)="clear()" aria-label="Clear search"
                    class="absolute right-1.5 top-1/2 flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-full text-fg-muted transition-colors hover:bg-surface-muted hover:text-fg">
                    <svg lucideX class="h-4 w-4"></svg>
                </button>
            }
        </label>
    `,
    host: { class: 'block' },
})
export class SearchInputComponent {
    readonly value = model('');
    readonly placeholder = input('Search…');

    private readonly input$ = new Subject<string>();

    constructor() {
        this.input$.pipe(debounceTime(150), takeUntilDestroyed(inject(DestroyRef))).subscribe((v) => this.value.set(v));
    }

    onInput(event: Event): void {
        this.input$.next((event.target as HTMLInputElement).value);
    }

    clear(): void {
        this.value.set('');
    }
}
