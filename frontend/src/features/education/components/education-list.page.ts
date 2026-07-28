import { DatePipe } from "@angular/common";
import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { LucideGraduationCap, LucideListFilter, LucideSearch, LucideX } from "@lucide/angular";
import { EducationListItem } from "../../admin/features/education/types";
import { EducationPublicService } from "../services/education.service";

const ALL = "All";

const LEVEL_LABELS: Record<string, string> = {
    HIGH_SCHOOL: "High School",
    ASSOCIATE: "Associate",
    BACHELOR: "Bachelor",
    MASTER: "Master",
    PHD: "PhD",
    CERTIFICATE: "Certificate",
    BOOTCAMP: "Bootcamp",
};

const STATUS_LABELS: Record<string, string> = {
    COMPLETED: "Completed",
    ONGOING: "Ongoing",
    DROPPED: "Dropped",
};

@Component({
    selector: "app-education-list",
    imports: [DatePipe, LucideGraduationCap, LucideListFilter, LucideSearch, LucideX],
    templateUrl: "../ui/education-list.page.html",
})
export class EducationListPage implements OnInit {
    private readonly educationService = inject(EducationPublicService);

    readonly loading = signal(true);
    readonly errorMessage = signal<string | null>(null);
    readonly items = signal<EducationListItem[]>([]);

    readonly searchTerm = signal("");
    readonly activeLevel = signal(ALL);
    readonly activeStatus = signal(ALL);
    readonly filtersOpen = signal(false);

    readonly levels = computed(() => {
        const values = new Set(this.items().map((i) => i.level));
        return [ALL, ...Array.from(values)];
    });

    readonly statuses = computed(() => {
        const values = new Set(this.items().map((i) => i.status));
        return [ALL, ...Array.from(values)];
    });

    readonly activeFilterCount = computed(() => {
        let count = 0;
        if (this.activeLevel() !== ALL) count++;
        if (this.activeStatus() !== ALL) count++;
        return count;
    });

    readonly visibleItems = computed(() => {
        const level = this.activeLevel();
        const status = this.activeStatus();
        const term = this.searchTerm().trim().toLowerCase();

        return this.items()
            .filter((i) => {
                if (level !== ALL && i.level !== level) return false;
                if (status !== ALL && i.status !== status) return false;
                if (term) {
                    const haystack = [i.institution, i.degree ?? "", i.fieldOfStudy, i.description ?? ""]
                        .join(" ")
                        .toLowerCase();
                    if (!haystack.includes(term)) return false;
                }
                return true;
            })
            .sort((a, b) => new Date(b.startDate).getTime() - new Date(a.startDate).getTime());
    });

    ngOnInit(): void {
        this.educationService.list().subscribe({
            next: (items) => {
                this.items.set(items);
                this.loading.set(false);
            },
            error: () => {
                this.errorMessage.set("Failed to load education history.");
                this.loading.set(false);
            },
        });
    }

    setLevel(level: string): void {
        this.activeLevel.set(level);
    }

    setStatus(status: string): void {
        this.activeStatus.set(status);
    }

    openFilters(): void {
        this.filtersOpen.set(true);
    }

    closeFilters(): void {
        this.filtersOpen.set(false);
    }

    clearFilters(): void {
        this.activeLevel.set(ALL);
        this.activeStatus.set(ALL);
    }

    clearSearch(): void {
        this.searchTerm.set("");
    }

    onSearchInput(event: Event): void {
        this.searchTerm.set((event.target as HTMLInputElement).value);
    }

    levelLabel(level: string): string {
        return LEVEL_LABELS[level] ?? level;
    }

    statusLabel(status: string): string {
        return STATUS_LABELS[status] ?? status;
    }
}
