import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { RouterLink } from "@angular/router";
import {
    LucideCode,
    LucideExternalLink,
    LucideListFilter,
    LucideFolderOpen,
    LucideSearch,
    LucideX,
} from "@lucide/angular";
import { ProjectListItem } from "../../admin/features/projects/types";
import { ProjectsPublicService } from "../services/projects.service";

const ALL = "All";

@Component({
    selector: "app-projects-list",
    imports: [RouterLink, LucideCode, LucideExternalLink, LucideListFilter, LucideFolderOpen, LucideSearch, LucideX],
    templateUrl: "../ui/projects-list.page.html",
})
export class ProjectsListPage implements OnInit {
    private readonly projectsService = inject(ProjectsPublicService);

    readonly loading = signal(true);
    readonly errorMessage = signal<string | null>(null);
    readonly projects = signal<ProjectListItem[]>([]);

    readonly searchTerm = signal("");
    readonly activeCategory = signal(ALL);
    readonly activeStatus = signal(ALL);
    readonly activeComplexity = signal(ALL);
    readonly filtersOpen = signal(false);

    readonly categories = computed(() => {
        const names = new Set(this.projects().map((p) => p.categoryName).filter((n): n is string => !!n));
        return [ALL, ...Array.from(names)];
    });

    readonly statuses = computed(() => {
        const names = new Set(this.projects().map((p) => p.status));
        return [ALL, ...Array.from(names)];
    });

    readonly complexities = computed(() => {
        const names = new Set(this.projects().map((p) => p.complexity));
        return [ALL, ...Array.from(names)];
    });

    readonly activeFilterCount = computed(() => {
        let count = 0;
        if (this.activeCategory() !== ALL) count++;
        if (this.activeStatus() !== ALL) count++;
        if (this.activeComplexity() !== ALL) count++;
        return count;
    });

    readonly visibleProjects = computed(() => {
        const category = this.activeCategory();
        const status = this.activeStatus();
        const complexity = this.activeComplexity();
        const term = this.searchTerm().trim().toLowerCase();

        return this.projects().filter((p) => {
            if (category !== ALL && p.categoryName !== category) return false;
            if (status !== ALL && p.status !== status) return false;
            if (complexity !== ALL && p.complexity !== complexity) return false;
            if (term) {
                const haystack = [p.title, p.description ?? "", ...p.tags, ...p.skills].join(" ").toLowerCase();
                if (!haystack.includes(term)) return false;
            }
            return true;
        });
    });

    ngOnInit(): void {
        this.projectsService.list().subscribe({
            next: (projects) => {
                this.projects.set(projects);
                this.loading.set(false);
            },
            error: () => {
                this.errorMessage.set("Failed to load projects.");
                this.loading.set(false);
            },
        });
    }

    setCategory(category: string): void {
        this.activeCategory.set(category);
    }

    setStatus(status: string): void {
        this.activeStatus.set(status);
    }

    setComplexity(complexity: string): void {
        this.activeComplexity.set(complexity);
    }

    openFilters(): void {
        this.filtersOpen.set(true);
    }

    closeFilters(): void {
        this.filtersOpen.set(false);
    }

    clearFilters(): void {
        this.activeCategory.set(ALL);
        this.activeStatus.set(ALL);
        this.activeComplexity.set(ALL);
    }

    clearSearch(): void {
        this.searchTerm.set("");
    }

    onSearchInput(event: Event): void {
        this.searchTerm.set((event.target as HTMLInputElement).value);
    }
}
