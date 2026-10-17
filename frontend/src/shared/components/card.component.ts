import { Component, computed, input } from '@angular/core';

/**
 * Base surface for all cards. `interactive` adds hover lift/shadow.
 * Apply layout classes on the host element (e.g. class="flex flex-col").
 */
@Component({
    selector: 'app-card',
    template: `<ng-content />`,
    host: { '[class]': 'classes()' },
})
export class CardComponent {
    readonly interactive = input(false);
    readonly padding = input<'none' | 'sm' | 'md' | 'lg'>('md');
    readonly accent = input(false);

    readonly classes = computed(() =>
        [
            'block rounded-card border bg-surface-raised shadow-elev-1 transition-[border-color,box-shadow,transform] duration-200 ease-out',
            this.accent() ? 'border-accent/40' : 'border-border',
            this.interactive() ? 'hover:border-accent/40 hover:shadow-elev-2 motion-safe:hover:-translate-y-0.5' : '',
            { none: '', sm: 'p-4', md: 'p-5 sm:p-6', lg: 'p-6 sm:p-8' }[this.padding()],
        ].join(' '),
    );
}
