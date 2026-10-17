import { Observable, shareReplay, tap } from 'rxjs';

/**
 * Session-scoped in-memory cache for GET observables.
 * The same key always returns the same replayed observable; a failed request evicts itself so a retry refetches.
 */
export class RequestCache {
    private readonly store = new Map<string, Observable<unknown>>();

    get<T>(key: string, factory: () => Observable<T>): Observable<T> {
        const hit = this.store.get(key) as Observable<T> | undefined;
        if (hit) return hit;
        const obs = factory().pipe(
            tap({ error: () => this.store.delete(key) }),
            shareReplay({ bufferSize: 1, refCount: false }),
        );
        this.store.set(key, obs);
        return obs;
    }

    clear(): void {
        this.store.clear();
    }
}
