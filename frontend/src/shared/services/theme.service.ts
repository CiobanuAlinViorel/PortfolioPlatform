import { Injectable, Inject, PLATFORM_ID, signal } from "@angular/core";
import { isPlatformBrowser } from '@angular/common';


@Injectable({ providedIn: "root" })
export class ThemeService {
    private readonly themeSignal = signal<'light' | 'dark'>('light');
    readonly theme = this.themeSignal.asReadonly();
    private isBrowser: boolean;

    constructor(@Inject(PLATFORM_ID) platformId: object) {
        this.isBrowser = isPlatformBrowser(platformId);
    }

    getTheme(): 'light' | 'dark' {
        return this.themeSignal();
    }

    setTheme(): void {
        if (!this.isBrowser) return;

        const stored = typeof localStorage !== 'undefined' ? localStorage.getItem('theme') : null;
        const theme = stored ? (stored as 'light' | 'dark') : 
            (window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
        this.applyTheme(theme);
    }

    toggleTheme(): void {
        if (!this.isBrowser) return;
        const newTheme = this.themeSignal() === 'light' ? 'dark' : 'light';
        if (typeof localStorage !== 'undefined') localStorage.setItem('theme', newTheme);
        this.applyTheme(newTheme);
    }

    private applyTheme(theme: 'light' | 'dark'): void {
        this.themeSignal.set(theme);
        document.documentElement.classList.toggle('dark', theme === 'dark');
    }
}