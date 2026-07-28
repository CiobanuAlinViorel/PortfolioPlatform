import { Injectable, NgZone, PLATFORM_ID, effect, inject } from "@angular/core";
import { isPlatformBrowser } from "@angular/common";
import { ThemeService } from "../../../../shared/services/theme.service";

// Same OAuth 2.0 Web client ID as the backend's GOOGLE_CLIENT_ID (backend/.env) —
// it's a public identifier, not a secret, so it's safe to embed in frontend code.
// Authorized JavaScript origin for it must include http://localhost:4200 for local dev.
export const GOOGLE_CLIENT_ID = "623747746990-7iqpsv07vn9dagqeequfdqo9471le2qq.apps.googleusercontent.com";

declare const google: any;

@Injectable({ providedIn: "root" })
export class GoogleIdentityService {
    private readonly platformId = inject(PLATFORM_ID);
    private readonly ngZone = inject(NgZone);
    private readonly themeService = inject(ThemeService);

    private initialized = false;
    private activeContainer: HTMLElement | null = null;
    private activeCallback: ((idToken: string) => void) | null = null;

    constructor() {
        // Google's button has no "follow the page theme" option, so re-render it
        // whenever the app's dark/light theme toggles.
        effect(() => {
            const isDark = this.themeService.theme() === "dark";
            if (this.activeContainer) {
                this.renderInto(this.activeContainer, isDark);
            }
        });
    }

    get isConfigured(): boolean {
        return !!GOOGLE_CLIENT_ID;
    }

    renderButton(container: HTMLElement, onCredential: (idToken: string) => void): void {
        if (!isPlatformBrowser(this.platformId) || !this.isConfigured) {
            return;
        }

        this.activeContainer = container;
        this.activeCallback = onCredential;

        const start = () => {
            if (typeof google === "undefined") {
                setTimeout(start, 100);
                return;
            }
            if (!this.initialized) {
                google.accounts.id.initialize({
                    client_id: GOOGLE_CLIENT_ID,
                    callback: (response: { credential: string }) =>
                        this.ngZone.run(() => this.activeCallback?.(response.credential)),
                });
                this.initialized = true;
            }
            this.renderInto(container, this.themeService.theme() === "dark");
        };
        start();
    }

    private renderInto(container: HTMLElement, isDark: boolean): void {
        if (typeof google === "undefined") return;
        container.innerHTML = "";
        google.accounts.id.renderButton(container, {
            theme: isDark ? "filled_black" : "outline",
            size: "large",
            width: 320,
        });
    }
}
