import { DatePipe } from "@angular/common";
import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { LucideAward, LucideBadgeCheck, LucideCopy, LucideExternalLink } from "@lucide/angular";
import { Certificate } from "../../admin/features/certificates/types";
import { CertificatesPublicService } from "../services/certificates.service";
import { PageHeaderComponent } from "../../../shared/components/page-header.component";
import { FilterBarComponent, FilterGroup, FilterSelection } from "../../../shared/components/filter-bar.component";
import { SearchInputComponent } from "../../../shared/components/search-input.component";
import { ChipComponent } from "../../../shared/components/chip.component";
import { BadgeComponent } from "../../../shared/components/badge.component";
import { CardSkeletonComponent } from "../../../shared/components/skeleton.component";
import { ErrorStateComponent } from "../../../shared/components/error-state.component";
import { EmptyStateComponent } from "../../../shared/components/empty-state.component";
import { RevealDirective } from "../../../shared/directives/reveal.directive";
import { ToastService } from "../../../shared/services/toast.service";
import { countBy, syncQueryParams } from "../../../shared/utils/query-state";
import { initials, tileStyle } from "../../../shared/utils/visual";

const SORTS = [
    { id: "", label: "Newest" },
    { id: "oldest", label: "Oldest" },
    { id: "az", label: "A – Z" },
];

export function isExpired(cert: Certificate): boolean {
    if (!cert.hasExpiry || !cert.expiryDate) return false;
    return new Date(cert.expiryDate).getTime() < Date.now();
}

@Component({
    selector: "app-certificates-list",
    imports: [
        DatePipe,
        LucideBadgeCheck,
        LucideCopy,
        LucideExternalLink,
        PageHeaderComponent,
        FilterBarComponent,
        SearchInputComponent,
        ChipComponent,
        BadgeComponent,
        CardSkeletonComponent,
        ErrorStateComponent,
        EmptyStateComponent,
        RevealDirective,
    ],
    templateUrl: "../ui/certificates-list.page.html",
})
export class CertificatesListPage implements OnInit {
    private readonly certificatesService = inject(CertificatesPublicService);
    private readonly toast = inject(ToastService);

    readonly loading = signal(true);
    readonly error = signal(false);
    readonly certificates = signal<Certificate[]>([]);

    readonly searchTerm = signal("");
    readonly filters = signal<FilterSelection>({});
    readonly sort = signal("");
    readonly validOnly = signal("");
    readonly sortOptions = SORTS;
    readonly emptyIcon = LucideAward;
    readonly isExpired = isExpired;
    readonly tileStyle = tileStyle;

    readonly showSearch = computed(() => this.certificates().length >= 6);
    readonly hasExpired = computed(() => this.certificates().some(isExpired));

    readonly filterGroups = computed((): FilterGroup[] => [
        { id: "category", label: "Category", options: countBy(this.certificates(), (c) => c.categoryName).map((o) => ({ ...o, label: o.id })).sort((a, b) => a.label.localeCompare(b.label)) },
        { id: "provider", label: "Provider", options: countBy(this.certificates(), (c) => c.provider).map((o) => ({ ...o, label: o.id })).sort((a, b) => a.label.localeCompare(b.label)) },
    ]);

    readonly visibleCertificates = computed(() => {
        const f = this.filters();
        const term = this.searchTerm().trim().toLowerCase();
        const validOnly = this.validOnly() === "1";
        const list = this.certificates().filter((c) => {
            if (f["category"] && c.categoryName !== f["category"]) return false;
            if (f["provider"] && c.provider !== f["provider"]) return false;
            if (validOnly && isExpired(c)) return false;
            if (term && ![c.name, c.provider, c.description ?? "", c.categoryName ?? ""].join(" ").toLowerCase().includes(term)) return false;
            return true;
        });
        switch (this.sort()) {
            case "oldest":
                return list.sort((a, b) => a.issueDate.localeCompare(b.issueDate));
            case "az":
                return list.sort((a, b) => a.name.localeCompare(b.name));
            default:
                return list.sort((a, b) => b.issueDate.localeCompare(a.issueDate));
        }
    });

    readonly hasActiveQuery = computed(() => !!this.searchTerm() || Object.keys(this.filters()).length > 0 || !!this.validOnly());

    constructor() {
        syncQueryParams({ q: this.searchTerm, sort: this.sort, valid: this.validOnly }, this.filters, ["category", "provider"]);
    }

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.loading.set(true);
        this.error.set(false);
        this.certificatesService.list().subscribe({
            next: (list) => { this.certificates.set(list); this.loading.set(false); },
            error: () => { this.error.set(true); this.loading.set(false); },
        });
    }

    providerInitials(provider: string): string {
        return initials(...provider.split(/\s+/)) || provider.slice(0, 2).toUpperCase();
    }

    toggleValidOnly(): void {
        this.validOnly.set(this.validOnly() === "1" ? "" : "1");
    }

    async copy(value: string): Promise<void> {
        try {
            await navigator.clipboard.writeText(value);
            this.toast.show("Credential ID copied", "success");
        } catch {
            this.toast.show("Couldn't copy to clipboard", "danger");
        }
    }

    clearAll(): void {
        this.searchTerm.set("");
        this.filters.set({});
        this.validOnly.set("");
    }
}
