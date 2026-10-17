import { A11yModule } from '@angular/cdk/a11y';
import { DOCUMENT } from '@angular/common';
import { Component, ElementRef, effect, inject, input, model, viewChild } from '@angular/core';
import { LucideX } from '@lucide/angular';

/**
 * Accessible overlay. `mode="sheet"` renders a bottom sheet (mobile filters);
 * `mode="dialog"` renders a centered dialog (lightbox etc.).
 * Two-way bind `open`; closes on Escape / backdrop click and restores focus to the previously focused element.
 */
@Component({
    selector: 'app-dialog',
    imports: [A11yModule, LucideX],
    template: `
        @if (open()) {
            <div class="fixed inset-0 z-[80] flex" [class]="mode() === 'sheet' ? 'items-end' : 'items-center justify-center p-4'"
                (keydown.escape)="close()">
                <div class="absolute inset-0 bg-black/40 backdrop-blur-sm motion-safe:animate-[fade-up_0.2s_ease-out]" (click)="close()" aria-hidden="true"></div>
                <div #panel role="dialog" aria-modal="true" [attr.aria-label]="title() || null" cdkTrapFocus cdkTrapFocusAutoCapture tabindex="-1"
                    class="relative z-10 flex max-h-[min(90dvh,100%)] w-full flex-col overflow-hidden bg-surface-overlay text-fg shadow-elev-3 outline-none motion-safe:animate-fade-up"
                    [class]="mode() === 'sheet'
                        ? 'rounded-t-panel pb-[env(safe-area-inset-bottom)]'
                        : 'rounded-panel ' + maxWidth()">
                    @if (mode() === 'sheet') {
                        <div class="mx-auto mt-3 h-1.5 w-12 shrink-0 rounded-full bg-border" aria-hidden="true"></div>
                    }
                    @if (title() || showClose()) {
                        <div class="flex shrink-0 items-center justify-between gap-4 px-5 pt-4" [class.pb-2]="!!title()">
                            @if (title()) {
                                <h2 class="font-brand text-lg font-semibold">{{ title() }}</h2>
                            } @else {
                                <span></span>
                            }
                            @if (showClose()) {
                                <button type="button" (click)="close()" aria-label="Close"
                                    class="flex h-10 w-10 items-center justify-center rounded-full text-fg-muted transition-colors hover:bg-surface-muted hover:text-fg">
                                    <svg lucideX class="h-5 w-5"></svg>
                                </button>
                            }
                        </div>
                    }
                    <div class="min-h-0 flex-1 overflow-y-auto px-5 pb-5" [class.pt-4]="!title() && !showClose()">
                        <ng-content />
                    </div>
                    <ng-content select="[footer]" />
                </div>
            </div>
        }
    `,
})
export class DialogComponent {
    readonly open = model(false);
    readonly mode = input<'sheet' | 'dialog'>('sheet');
    readonly title = input('');
    readonly showClose = input(true);
    readonly maxWidth = input('max-w-lg');

    private readonly panel = viewChild<ElementRef<HTMLElement>>('panel');
    private readonly document = inject(DOCUMENT);
    private previouslyFocused: HTMLElement | null = null;

    constructor() {
        effect(() => {
            const isOpen = this.open();
            const body = this.document.body;
            if (!body) return;
            if (isOpen) {
                this.previouslyFocused = (this.document.activeElement as HTMLElement | null) ?? null;
                body.classList.add('scroll-locked');
            } else {
                body.classList.remove('scroll-locked');
                this.previouslyFocused?.focus?.();
                this.previouslyFocused = null;
            }
        });
    }

    close(): void {
        this.open.set(false);
    }
}
