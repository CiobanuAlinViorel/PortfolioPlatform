import { Component, computed, inject, signal } from "@angular/core";
import { RouterLink, RouterLinkActive } from "@angular/router";
import {
    LucideBriefcase,
    LucideDynamicIcon,
    LucideFolderKanban,
    LucideHouse,
    LucideIcon,
    LucideMail,
    LucideMenu,
    LucideMoon,
    LucideShieldCheck,
    LucideSun,
    LucideUser,
    LucideX,
} from "@lucide/angular";
import { ThemeService } from "../services/theme.service";
import { AuthService } from "../../features/auth/register/services/auth.service";

interface NavLink {
    label: string;
    path: string;
    icon: LucideIcon;
}

@Component({
    selector: "app-header",
    imports: [
        RouterLink,
        RouterLinkActive,
        LucideSun,
        LucideMoon,
        LucideMenu,
        LucideX,
        LucideDynamicIcon,
        LucideShieldCheck,
    ],
    templateUrl: "../ui/header.component.html"
})
export class HeaderComponent {
    title = "Portfolio";

    readonly navLinks: NavLink[] = [
        { label: "Home", path: "/", icon: LucideHouse },
        { label: "Projects", path: "/projects", icon: LucideFolderKanban },
        { label: "Experience", path: "/experience", icon: LucideBriefcase },
        { label: "About", path: "/about", icon: LucideUser },
        { label: "Contact", path: "/contact", icon: LucideMail },
    ];

    private readonly themeService = inject(ThemeService);
    private readonly authService = inject(AuthService);

    private readonly isMenuOpenSignal = signal(false);
    isMenuOpen = this.isMenuOpenSignal.asReadonly();

    isDarkMode = computed(() => this.themeService.theme() === 'dark');

    isAuthenticated = computed(() => this.authService.isAuthenticated());
    isAdmin = computed(() => this.authService.currentUser()?.role === 'ADMIN');

    toggleMenu(): void {
        this.isMenuOpenSignal.update((open) => !open);
    }

    closeMenu(): void {
        this.isMenuOpenSignal.set(false);
    }

    logout(): void {
        this.authService.logout().subscribe({
            next: () => {
                this.closeMenu();
            }
        });
    }

    toggleTheme(): void {
        this.themeService.toggleTheme();
    }
}
