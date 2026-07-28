import { Component, signal } from "@angular/core";
import {
    LucideMenu,
    LucideX,
    LucideLayoutDashboard,
    LucideSettings,
    LucideGraduationCap,
    LucideBriefcase,
    LucideFolderKanban,
    LucideSparkles,
    LucideAward,
    LucideHeart,
    LucideHandHeart,
    LucideHouse,
} from "@lucide/angular";
import { RouterLink, RouterLinkActive } from "@angular/router";

@Component({
    selector: 'app-sidebar',
    imports: [
        LucideMenu,
        LucideX,
        LucideLayoutDashboard,
        LucideSettings,
        LucideGraduationCap,
        LucideBriefcase,
        LucideFolderKanban,
        LucideSparkles,
        LucideAward,
        LucideHeart,
        LucideHandHeart,
        LucideHouse,
        RouterLink,
        RouterLinkActive,
    ],
    templateUrl: '../ui/sidebar.component.html',
})
export class SidebarComponent {
    readonly isOpen = signal(false);

    toggle(): void {
        this.isOpen.update((open) => !open);
    }

    close(): void {
        this.isOpen.set(false);
    }
}
