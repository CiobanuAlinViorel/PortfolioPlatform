import { Directive, ElementRef, OnDestroy, OnInit, PLATFORM_ID, inject, input } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

/**
 * Adds `.reveal` on init and `.is-visible` once the element enters the viewport.
 * `appRevealIndex` sets the `--i` CSS variable for staggered children.
 */
@Directive({
    selector: '[appReveal]',
    host: { class: 'reveal', '[style.--i]': 'appRevealIndex()' },
})
export class RevealDirective implements OnInit, OnDestroy {
    readonly appRevealIndex = input<number>(0);

    private readonly el = inject<ElementRef<HTMLElement>>(ElementRef);
    private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
    private observer: IntersectionObserver | null = null;

    ngOnInit(): void {
        const node = this.el.nativeElement;
        if (!this.isBrowser || typeof IntersectionObserver === 'undefined') {
            node.classList.add('is-visible');
            return;
        }
        this.observer = new IntersectionObserver(
            (entries) => {
                if (entries.some((e) => e.isIntersecting)) {
                    node.classList.add('is-visible');
                    this.observer?.disconnect();
                }
            },
            { rootMargin: '0px 0px -8% 0px', threshold: 0.05 },
        );
        this.observer.observe(node);
    }

    ngOnDestroy(): void {
        this.observer?.disconnect();
    }
}
