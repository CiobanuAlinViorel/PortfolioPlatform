import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { LucideHeart, LucideListFilter, LucideSearch, LucideX } from "@lucide/angular";
import { HobbyListItem } from "../../admin/features/hobbies/types";
import { HobbiesPublicService } from "../services/hobbies.service";

const ALL = "All";

function titleCase(value: string): string {
    return value
        .toLowerCase()
        .split("_")
        .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
        .join(" ");
}

@Component({
    selector: "app-hobbies-list",
    imports: [LucideHeart, LucideListFilter, LucideSearch, LucideX],
    templateUrl: "../ui/hobbies-list.page.html",
})
export class HobbiesListPage implements OnInit {
    private readonly hobbiesService = inject(HobbiesPublicService);

    readonly loading = signal(true);
    readonly errorMessage = signal<string | null>(null);
    readonly hobbies = signal<HobbyListItem[]>([]);

    readonly searchTerm = signal("");
    readonly activeCategory = signal(ALL);
    readonly filtersOpen = signal(false);

    readonly categories = computed(() => {
        const values = new Set(this.hobbies().map((h) => h.category));
        return [ALL, ...Array.from(values)];
    });

    readonly activeFilterCount = computed(() => (this.activeCategory() !== ALL ? 1 : 0));

    readonly visibleHobbies = computed(() => {
        const category = this.activeCategory();
        const term = this.searchTerm().trim().toLowerCase();

        return this.hobbies().filter((h) => {
            if (category !== ALL && h.category !== category) return false;
            if (term) {
                const haystack = [h.name, h.description ?? "", h.whyInterested ?? "", h.favoriteAspect ?? ""]
                    .join(" ")
                    .toLowerCase();
                if (!haystack.includes(term)) return false;
            }
            return true;
        });
    });

    ngOnInit(): void {
        this.hobbiesService.list().subscribe({
            next: (hobbies) => {
                this.hobbies.set(hobbies);
                this.loading.set(false);
            },
            error: () => {
                this.errorMessage.set("Failed to load hobbies.");
                this.loading.set(false);
            },
        });
    }

    setCategory(category: string): void {
        this.activeCategory.set(category);
    }

    openFilters(): void {
        this.filtersOpen.set(true);
    }

    closeFilters(): void {
        this.filtersOpen.set(false);
    }

    clearFilters(): void {
        this.activeCategory.set(ALL);
    }

    clearSearch(): void {
        this.searchTerm.set("");
    }

    onSearchInput(event: Event): void {
        this.searchTerm.set((event.target as HTMLInputElement).value);
    }

    label(value: string): string {
        return value === ALL ? ALL : titleCase(value);
    }
}
