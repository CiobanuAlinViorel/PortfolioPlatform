import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideLoaderCircle } from '@lucide/angular';

export type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'link';
export type ButtonSize = 'sm' | 'md' | 'lg';

const BASE =
    'group inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-control font-semibold transition-[color,background-color,border-color,box-shadow,transform] duration-200 ease-out active:scale-[0.98] disabled:pointer-events-none disabled:opacity-60';

const VARIANTS: Record<ButtonVariant, string> = {
    primary: 'bg-accent text-accent-fg shadow-elev-1 hover:bg-accent-hover hover:shadow-elev-2',
    secondary: 'border border-border bg-surface-raised text-fg shadow-elev-1 hover:border-accent/40 hover:bg-surface-overlay hover:shadow-elev-2',
    ghost: 'text-fg hover:bg-surface-muted',
    link: 'text-accent underline-offset-4 hover:underline px-0',
};

const SIZES: Record<ButtonSize, string> = {
    sm: 'h-9 px-3.5 text-sm',
    md: 'h-11 px-5 text-sm',
    lg: 'h-12 px-6 text-base',
};

/**
 * Button or link (when `routerLink`/`href` is set) with consistent variants.
 * Usage: <app-button variant="primary" link="/projects">View projects</app-button>
 */
@Component({
    selector: 'app-button',
    imports: [NgTemplateOutlet, RouterLink, LucideLoaderCircle],
    template: `
        @if (link()) {
            <a [routerLink]="link()" [fragment]="fragment()" [class]="classes()" [attr.aria-label]="ariaLabel()">
                <ng-container *ngTemplateOutlet="content" />
            </a>
        } @else if (href()) {
            <a [href]="href()" [target]="target()" [rel]="target() === '_blank' ? 'noopener noreferrer' : null" [class]="classes()" [attr.aria-label]="ariaLabel()">
                <ng-container *ngTemplateOutlet="content" />
            </a>
        } @else {
            <button [type]="type()" [disabled]="disabled() || loading()" [class]="classes()" [attr.aria-label]="ariaLabel()" [attr.aria-busy]="loading() || null">
                <ng-container *ngTemplateOutlet="content" />
            </button>
        }

        <ng-template #content>
            @if (loading()) {
                <svg lucideLoaderCircle class="h-4 w-4 shrink-0 animate-spin"></svg>
            }
            <ng-content />
        </ng-template>
    `,
    host: { class: 'inline-flex max-w-full', '[class.w-full]': 'block()' },
})
export class ButtonComponent {
    readonly variant = input<ButtonVariant>('primary');
    readonly size = input<ButtonSize>('md');
    readonly type = input<'button' | 'submit' | 'reset'>('button');
    readonly disabled = input(false);
    readonly loading = input(false);
    readonly block = input(false);
    readonly link = input<string | unknown[] | null>(null);
    readonly fragment = input<string | undefined>(undefined);
    readonly href = input<string | null>(null);
    readonly target = input<string>('_self');
    readonly ariaLabel = input<string | null>(null);
    readonly extraClass = input('');

    readonly classes = computed(() =>
        [BASE, VARIANTS[this.variant()], this.variant() === 'link' ? '' : SIZES[this.size()], this.block() ? 'w-full' : '', this.extraClass()]
            .filter(Boolean)
            .join(' '),
    );
}
