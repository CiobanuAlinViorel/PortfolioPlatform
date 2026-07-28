import { DatePipe } from "@angular/common";
import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { RouterLink } from "@angular/router";
import {
    LucideCalendar, LucideExternalLink, LucideHeartHandshake, LucideListFilter, LucideMapPin, LucideSearch, LucideX,
} from "@lucide/angular";
import { VolunteerExperienceListItem } from "../../../admin/features/volunteer-experiences/types";
import { VolunteerExperiencesPublicService } from "../services/volunteer-experiences.service";

const ALL = "All";

@Component({
    selector: "app-volunteer-list",
    imports: [
        RouterLink, DatePipe, LucideCalendar, LucideExternalLink, LucideHeartHandshake, LucideListFilter, LucideMapPin,
        LucideSearch, LucideX,
    ],
    templateUrl: "../ui/volunteer-list.page.html",
})
export class VolunteerListPage implements OnInit {
    private readonly volunteerService = inject(VolunteerExperiencesPublicService);

    readonly loading = signal(true);
    readonly errorMessage = signal<string | null>(null);
    readonly items = signal<VolunteerExperienceListItem[]>([]);

    readonly searchTerm = signal("");
    readonly activeType = signal(ALL);
    readonly activeStatus = signal(ALL);
    readonly filtersOpen = signal(false);

    readonly types = computed(() => {
        const names = new Set(this.items().map((i) => i.type));
        return [ALL, ...Array.from(names)];
    });
    readonly statuses = computed(() => {
        const names = new Set(this.items().map((i) => i.status));
        return [ALL, ...Array.from(names)];
    });
    readonly activeFilterCount = computed(() => {
        let count = 0;
        if (this.activeType() !== ALL) count++;
        if (this.activeStatus() !== ALL) count++;
        return count;
    });

    readonly visibleItems = computed(() => {
        const type = this.activeType();
        const status = this.activeStatus();
        const term = this.searchTerm().trim().toLowerCase();
        return this.items().filter((item) => {
            if (type !== ALL && item.type !== type) return false;
            if (status !== ALL && item.status !== status) return false;
            if (term) {
                const haystack = [item.organization, item.role, item.location ?? ""].join(" ").toLowerCase();
                if (!haystack.includes(term)) return false;
            }
            return true;
        });
    });

    ngOnInit(): void {
        this.volunteerService.list().subscribe({
            next: (items) => { this.items.set(items); this.loading.set(false); },
            error: () => { this.errorMessage.set("Failed to load volunteer experience."); this.loading.set(false); },
        });
    }

    setType(type: string): void { this.activeType.set(type); }
    setStatus(status: string): void { this.activeStatus.set(status); }
    openFilters(): void { this.filtersOpen.set(true); }
    closeFilters(): void { this.filtersOpen.set(false); }
    clearFilters(): void { this.activeType.set(ALL); this.activeStatus.set(ALL); }
    clearSearch(): void { this.searchTerm.set(""); }
    onSearchInput(event: Event): void { this.searchTerm.set((event.target as HTMLInputElement).value); }
}
