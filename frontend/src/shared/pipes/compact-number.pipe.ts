import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'compactNumber' })
export class CompactNumberPipe implements PipeTransform {
    transform(value: number | string | null | undefined): string {
        if (value === null || value === undefined || value === '') return '—';
        const n = typeof value === 'string' ? Number(value) : value;
        if (Number.isNaN(n)) return String(value);
        if (Math.abs(n) >= 1_000_000) return `${trim(n / 1_000_000)}M`;
        if (Math.abs(n) >= 1_000) return `${trim(n / 1_000)}K`;
        return trim(n);
    }
}

function trim(n: number): string {
    return n.toFixed(1).replace(/\.0$/, '');
}
