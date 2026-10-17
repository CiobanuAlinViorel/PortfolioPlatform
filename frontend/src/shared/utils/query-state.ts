import { isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID, WritableSignal, effect, inject, untracked } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FilterSelection } from '../components/filter-bar.component';

/**
 * Two-way sync between URL query params and list-page state.
 * Must be called in an injection context (field initializer / constructor).
 *
 * - `scalars`: param name -> string signal (empty string removes the param)
 * - `filters`: a FilterSelection signal whose keys are written as individual params
 */
export function syncQueryParams(scalars: Record<string, WritableSignal<string>>, filters?: WritableSignal<FilterSelection>, filterKeys: string[] = []): void {
    const router = inject(Router);
    const route = inject(ActivatedRoute);
    const isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

    const initial = route.snapshot.queryParamMap;
    for (const [key, sig] of Object.entries(scalars)) {
        const v = initial.get(key);
        if (v) sig.set(v);
    }
    if (filters) {
        const sel: FilterSelection = {};
        for (const key of filterKeys) {
            const v = initial.get(key);
            if (v) sel[key] = v;
        }
        if (Object.keys(sel).length) filters.set(sel);
    }

    if (!isBrowser) return;

    effect(() => {
        const params: Record<string, string | null> = {};
        for (const [key, sig] of Object.entries(scalars)) params[key] = sig() || null;
        if (filters) {
            const sel = filters();
            for (const key of filterKeys) params[key] = sel[key] || null;
        }
        untracked(() => {
            router.navigate([], { relativeTo: route, queryParams: params, queryParamsHandling: 'merge', replaceUrl: true });
        });
    });
}

/** Count occurrences of a key across items, in first-seen order. */
export function countBy<T>(items: T[], key: (item: T) => string | null | undefined): Array<{ id: string; count: number }> {
    const map = new Map<string, number>();
    for (const item of items) {
        const k = key(item);
        if (!k) continue;
        map.set(k, (map.get(k) ?? 0) + 1);
    }
    return Array.from(map, ([id, count]) => ({ id, count }));
}
