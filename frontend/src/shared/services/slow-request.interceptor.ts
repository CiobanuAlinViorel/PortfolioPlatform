import { HttpInterceptorFn } from '@angular/common/http';
import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { finalize } from 'rxjs';

const SLOW_THRESHOLD_MS = 3000;

@Injectable({ providedIn: 'root' })
export class SlowRequestState {
    private pending = 0;
    private timer: ReturnType<typeof setTimeout> | null = null;
    private readonly slowSignal = signal(false);
    private readonly dismissedSignal = signal(false);

    readonly isSlow = this.slowSignal.asReadonly();
    readonly dismissed = this.dismissedSignal.asReadonly();

    start(): void {
        this.pending++;
        if (this.pending === 1 && !this.timer) {
            this.timer = setTimeout(() => this.slowSignal.set(true), SLOW_THRESHOLD_MS);
        }
    }

    end(): void {
        this.pending = Math.max(0, this.pending - 1);
        if (this.pending === 0) {
            if (this.timer) clearTimeout(this.timer);
            this.timer = null;
            this.slowSignal.set(false);
        }
    }

    dismiss(): void {
        this.dismissedSignal.set(true);
    }
}

export const slowRequestInterceptor: HttpInterceptorFn = (req, next) => {
    const platformId = inject(PLATFORM_ID);
    if (!isPlatformBrowser(platformId)) return next(req);
    const state = inject(SlowRequestState);
    state.start();
    return next(req).pipe(finalize(() => state.end()));
};
