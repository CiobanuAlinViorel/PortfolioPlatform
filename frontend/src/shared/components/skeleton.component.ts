import { Component, input } from '@angular/core';

/** Shimmer block. Size it with host classes: <app-skeleton class="h-6 w-1/2" /> */
@Component({
    selector: 'app-skeleton',
    template: ``,
    host: { class: 'block shimmer rounded-lg', '[class.rounded-full]': 'round()', 'aria-hidden': 'true' },
})
export class SkeletonComponent {
    readonly round = input(false);
}

@Component({
    selector: 'app-card-skeleton',
    imports: [SkeletonComponent],
    template: `
        <div class="flex flex-col gap-4 rounded-card border border-border bg-surface-raised p-5 shadow-elev-1" aria-hidden="true">
            @if (image()) {
                <app-skeleton class="aspect-video w-full rounded-xl" />
            }
            <div class="space-y-2.5">
                <app-skeleton class="h-5 w-3/4" />
                <app-skeleton class="h-4 w-full" />
                <app-skeleton class="h-4 w-2/3" />
            </div>
            <div class="flex gap-2">
                <app-skeleton class="h-6 w-16 rounded-full" />
                <app-skeleton class="h-6 w-20 rounded-full" />
            </div>
        </div>
    `,
})
export class CardSkeletonComponent {
    readonly image = input(true);
}
