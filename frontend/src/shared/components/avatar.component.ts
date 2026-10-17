import { Component, computed, input, signal } from '@angular/core';
import { initials, tileStyle } from '../utils/visual';

/** Image avatar with initials fallback (missing URL or load error). */
@Component({
    selector: 'app-avatar',
    template: `
        @if (src() && !failed()) {
            <img [src]="src()" [alt]="alt()" [attr.fetchpriority]="priority() ? 'high' : null"
                [attr.loading]="priority() ? 'eager' : 'lazy'" decoding="async" (error)="failed.set(true)"
                class="h-full w-full rounded-full object-cover" />
        } @else {
            <span class="flex h-full w-full items-center justify-center rounded-full font-brand font-semibold"
                [style]="style()" [style.font-size.px]="fontSize()" [attr.aria-label]="alt()" role="img">
                {{ text() }}
            </span>
        }
    `,
    host: { class: 'block shrink-0 overflow-hidden rounded-full', '[style.width.px]': 'size()', '[style.height.px]': 'size()' },
})
export class AvatarComponent {
    readonly src = input<string | null | undefined>(null);
    readonly name = input('');
    readonly alt = input('');
    readonly size = input(48);
    readonly priority = input(false);

    readonly failed = signal(false);
    readonly text = computed(() => initials(...this.name().split(' ')) || '?');
    readonly style = computed(() => tileStyle(this.name()));
    readonly fontSize = computed(() => Math.round(this.size() * 0.38));
}
