import { DatePipe } from "@angular/common";
import { Component, OnInit, computed, inject, signal } from "@angular/core";
import {
    LucideAward,
    LucideBadgeCheck,
    LucideExternalLink,
    LucideListFilter,
    LucideSearch,
    LucideX,
} from "@lucide/angular";
import { Certificate } from "../../admin/features/certificates/types";
import { CertificatesPublicService } from "../services/certificates.service";

const ALL = "All";

function isExpired(cert: Certificate): boolean {
    if (!cert.hasExpiry || !cert.expiryDate) return false;
    return new Date(cert.expiryDate).getTime() < Date.now();
}

@Component({
    selector: "app-certificates-list",
    imports: [DatePipe, LucideAward, LucideBadgeCheck, LucideExternalLink, LucideListFilter, LucideSearch, LucideX],
    templateUrl: "../ui/certificates-list.page.html",
})
export class CertificatesListPage implements OnInit {
    private readonly certificatesService = inject(CertificatesPublicService);

    readonly loading = signal(true);
    readonly errorMessage = signal<string | null>(null);
    readonly certificates = signal<Certificate[]>([]);

    readonly searchTerm = signal("");
    readonly activeCategory = signal(ALL);
    readonly activeProvider = signal(ALL);
    readonly filtersOpen = signal(false);

    readonly categories = computed(() => {
        const names = new Set(this.certificates().map((c) => c.categoryName).filter((n): n is string => !!n));
        return [ALL, ...Array.from(names).sort()];
    });

    readonly providers = computed(() => {
        const names = new Set(this.certificates().map((c) => c.provider));
        return [ALL, ...Array.from(names).sort()];
    });

    readonly activeFilterCount = computed(() => {
        let count = 0;
        if (this.activeCategory() !== ALL) count++;
        if (this.activeProvider() !== ALL) count++;
        return count;
    });

    readonly visibleCertificates = computed(() => {
        const category = this.activeCategory();
        const provider = this.activeProvider();
        const term = this.searchTerm().trim().toLowerCase();

        return this.certificates()
            .filter((c) => {
                if (category !== ALL && c.categoryName !== category) return false;
                if (provider !== ALL && c.provider !== provider) return false;
                if (term) {
                    const haystack = [c.name, c.provider, c.description ?? "", c.categoryName ?? ""]
                        .join(" ")
                        .toLowerCase();
                    if (!haystack.includes(term)) return false;
                }
                return true;
            })
            .sort((a, b) => new Date(b.issueDate).getTime() - new Date(a.issueDate).getTime());
    });

    ngOnInit(): void {
        this.certificatesService.list().subscribe({
            next: (certificates) => {
                this.certificates.set(certificates);
                this.loading.set(false);
            },
            error: () => {
                this.errorMessage.set("Failed to load certificates.");
                this.loading.set(false);
            },
        });
    }

    setCategory(category: string): void {
        this.activeCategory.set(category);
    }

    setProvider(provider: string): void {
        this.activeProvider.set(provider);
    }

    openFilters(): void {
        this.filtersOpen.set(true);
    }

    closeFilters(): void {
        this.filtersOpen.set(false);
    }

    clearFilters(): void {
        this.activeCategory.set(ALL);
        this.activeProvider.set(ALL);
    }

    clearSearch(): void {
        this.searchTerm.set("");
    }

    onSearchInput(event: Event): void {
        this.searchTerm.set((event.target as HTMLInputElement).value);
    }

    isExpired(cert: Certificate): boolean {
        return isExpired(cert);
    }
}
