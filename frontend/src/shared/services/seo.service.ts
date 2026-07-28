import { DOCUMENT } from "@angular/common";
import { Injectable, inject } from "@angular/core";
import { Meta, Title } from "@angular/platform-browser";

export const SITE_NAME = "Alin-Viorel Ciobanu";
export const SITE_URL = "https://alinviorelciobanu.com";
export const DEFAULT_OG_IMAGE = `${SITE_URL}/logo.png`;

export interface SeoData {
    /** Page-specific title fragment, e.g. "Projects" or a project's own title. */
    title: string;
    /** ~150-160 chars, shown in search result snippets. */
    description: string;
    /** Absolute image URL; falls back to the site logo. */
    image?: string;
    /** Marks the page as noindex,nofollow (admin/auth pages). */
    noindex?: boolean;
    /** Set false when `title` is already the full brand string (Home page). */
    suffixTitle?: boolean;
}

@Injectable({ providedIn: "root" })
export class SeoService {
    private readonly titleService = inject(Title);
    private readonly meta = inject(Meta);
    private readonly document = inject(DOCUMENT);

    update(data: SeoData, path: string): void {
        const fullTitle = data.suffixTitle === false ? data.title : `${data.title} | ${SITE_NAME}`;
        const image = data.image ?? DEFAULT_OG_IMAGE;
        const url = `${SITE_URL}${path}`;

        this.titleService.setTitle(fullTitle);

        this.setTag("name", "description", data.description);
        this.setTag("name", "robots", data.noindex ? "noindex, nofollow" : "index, follow");

        this.setTag("property", "og:title", fullTitle);
        this.setTag("property", "og:description", data.description);
        this.setTag("property", "og:type", "website");
        this.setTag("property", "og:url", url);
        this.setTag("property", "og:image", image);
        this.setTag("property", "og:site_name", SITE_NAME);

        this.setTag("name", "twitter:card", "summary_large_image");
        this.setTag("name", "twitter:title", fullTitle);
        this.setTag("name", "twitter:description", data.description);
        this.setTag("name", "twitter:image", image);

        this.setCanonical(url);
    }

    truncate(text: string, max = 155): string {
        if (text.length <= max) return text;
        return `${text.slice(0, max - 1).trimEnd()}…`;
    }

    private setTag(attr: "name" | "property", key: string, content: string): void {
        this.meta.updateTag({ [attr]: key, content });
    }

    private setCanonical(url: string): void {
        let link = this.document.querySelector<HTMLLinkElement>('link[rel="canonical"]');
        if (!link) {
            link = this.document.createElement("link");
            link.setAttribute("rel", "canonical");
            this.document.head.appendChild(link);
        }
        link.setAttribute("href", url);
    }
}
