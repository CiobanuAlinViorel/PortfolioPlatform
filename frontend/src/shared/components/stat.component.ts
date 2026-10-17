import { Component, ElementRef, OnDestroy, OnInit, PLATFORM_ID, computed, inject, input, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

/** Big number + label. Numeric values count up once the stat scrolls into view. */
@Component({
    selector: 'app-stat',
    template: `
        <div class="font-brand text-4xl font-semibold tabular-nums tracking-tight text-fg sm:text-5xl" [class.text-accent]="accent()">
            {{ display() }}<span class="text-accent">{{ suffix() }}</span>
        </div>
        <p class="mt-1.5 text-xs font-medium uppercase tracking-[0.14em] text-fg-muted">{{ label() }}</p>
    `,
    host: { class: 'block' },
})
export class StatComponent implements OnInit, OnDestroy {
    readonly value = input.required<number | string>();
    readonly label = input.required<string>();
    readonly suffix = input('');
    readonly accent = input(false);

    private readonly el = inject<ElementRef<HTMLElement>>(ElementRef);
    private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
    private readonly current = signal<number | null>(null);
    private observer: IntersectionObserver | null = null;
    private frame = 0;

    readonly display = computed(() => {
        const v = this.value();
        if (typeof v !== 'number') return v;
        const c = this.current();
        return (c ?? v).toLocaleString();
    });

    ngOnInit(): void {
        const v = this.value();
        if (typeof v !== 'number' || !this.isBrowser || typeof IntersectionObserver === 'undefined') return;
        if (window.matchMedia?.('(prefers-reduced-motion: reduce)').matches) return;
        this.current.set(0);
        this.observer = new IntersectionObserver((entries) => {
            if (entries.some((e) => e.isIntersecting)) {
                this.observer?.disconnect();
                this.animate(v);
            }
        }, { threshold: 0.4 });
        this.observer.observe(this.el.nativeElement);
    }

    private animate(target: number): void {
        const start = performance.now();
        const duration = 900;
        const step = (now: number) => {
            const t = Math.min(1, (now - start) / duration);
            const eased = 1 - Math.pow(1 - t, 4);
            this.current.set(Math.round(target * eased));
            if (t < 1) this.frame = requestAnimationFrame(step);
        };
        this.frame = requestAnimationFrame(step);
    }

    ngOnDestroy(): void {
        this.observer?.disconnect();
        if (this.frame) cancelAnimationFrame(this.frame);
    }
}
