import { Component, OnInit, computed, inject, signal } from "@angular/core";
import {
    LucideAward,
    LucideGraduationCap,
    LucideLayers,
    LucideListFilter,
    LucideSearch,
    LucideX,
} from "@lucide/angular";
import { ProficiencyLevel, SkillListItem } from "../../admin/features/skills/types";
import { SkillsPublicService } from "../services/skills.service";

const ALL = "All";

const PROFICIENCY_ORDER: Record<ProficiencyLevel, number> = {
    BEGINNER: 0,
    INTERMEDIATE: 1,
    ADVANCED: 2,
    EXPERT: 3,
};

@Component({
    selector: "app-skills-list",
    imports: [LucideAward, LucideGraduationCap, LucideLayers, LucideListFilter, LucideSearch, LucideX],
    templateUrl: "../ui/skills-list.page.html",
})
export class SkillsListPage implements OnInit {
    private readonly skillsService = inject(SkillsPublicService);

    readonly loading = signal(true);
    readonly errorMessage = signal<string | null>(null);
    readonly skills = signal<SkillListItem[]>([]);

    readonly searchTerm = signal("");
    readonly activeCategory = signal(ALL);
    readonly activeProficiency = signal(ALL);
    readonly filtersOpen = signal(false);

    readonly categories = computed(() => {
        const names = new Set(this.skills().map((s) => s.categoryName).filter((n): n is string => !!n));
        return [ALL, ...Array.from(names).sort()];
    });

    readonly proficiencies = computed(() => {
        const names = new Set(this.skills().map((s) => s.proficiency));
        return [ALL, ...Array.from(names).sort((a, b) => PROFICIENCY_ORDER[a] - PROFICIENCY_ORDER[b])];
    });

    readonly activeFilterCount = computed(() => {
        let count = 0;
        if (this.activeCategory() !== ALL) count++;
        if (this.activeProficiency() !== ALL) count++;
        return count;
    });

    readonly visibleSkills = computed(() => {
        const category = this.activeCategory();
        const proficiency = this.activeProficiency();
        const term = this.searchTerm().trim().toLowerCase();

        return this.skills()
            .filter((s) => {
                if (category !== ALL && s.categoryName !== category) return false;
                if (proficiency !== ALL && s.proficiency !== proficiency) return false;
                if (term) {
                    const haystack = [s.name, s.description ?? "", s.categoryName ?? ""].join(" ").toLowerCase();
                    if (!haystack.includes(term)) return false;
                }
                return true;
            })
            .sort((a, b) => PROFICIENCY_ORDER[b.proficiency] - PROFICIENCY_ORDER[a.proficiency] || a.name.localeCompare(b.name));
    });

    ngOnInit(): void {
        this.skillsService.list().subscribe({
            next: (skills) => {
                this.skills.set(skills);
                this.loading.set(false);
            },
            error: () => {
                this.errorMessage.set("Failed to load skills.");
                this.loading.set(false);
            },
        });
    }

    setCategory(category: string): void {
        this.activeCategory.set(category);
    }

    setProficiency(proficiency: string): void {
        this.activeProficiency.set(proficiency);
    }

    openFilters(): void {
        this.filtersOpen.set(true);
    }

    closeFilters(): void {
        this.filtersOpen.set(false);
    }

    clearFilters(): void {
        this.activeCategory.set(ALL);
        this.activeProficiency.set(ALL);
    }

    clearSearch(): void {
        this.searchTerm.set("");
    }

    onSearchInput(event: Event): void {
        this.searchTerm.set((event.target as HTMLInputElement).value);
    }

    formatLabel(value: string): string {
        return value.charAt(0) + value.slice(1).toLowerCase();
    }
}
