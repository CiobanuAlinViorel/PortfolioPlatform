import { Component, ElementRef, HostListener, computed, inject, signal } from "@angular/core";
import { RouterLink, RouterLinkActive } from "@angular/router";
import {
    LucideAward,
    LucideBriefcase,
    LucideChevronDown,
    LucideDownload,
    LucideDynamicIcon,
    LucideFolderKanban,
    LucideGraduationCap,
    LucideHeart,
    LucideHeartHandshake,
    LucideHouse,
    LucideIcon,
    LucideLayers,
    LucideLoaderCircle,
    LucideMenu,
    LucideMoon,
    LucideShieldCheck,
    LucideSun,
    LucideX,
} from "@lucide/angular";
import { ThemeService } from "../services/theme.service";
import { AuthService } from "../../features/auth/register/services/auth.service";
import { CvDownloadService } from "../services/cv-download.service";

interface NavLink {
    label: string;
    path: string;
    icon: LucideIcon;
}

interface ExperienceSubLink {
    label: string;
    path: string;
    icon: LucideIcon;
}

interface AboutSubLink {
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
        LucideChevronDown,
        LucideDownload,
        LucideLoaderCircle,
    ],
    templateUrl: "../ui/header.component.html"
})
export class HeaderComponent {
    title = "Portfolio";

    readonly navLinks: NavLink[] = [
        { label: "Home", path: "/", icon: LucideHouse },
        { label: "Projects", path: "/projects", icon: LucideFolderKanban },
        { label: "Skills", path: "/skills", icon: LucideLayers },

    ];

    readonly experienceLinks: ExperienceSubLink[] = [
        { label: "Jobs", path: "/experience/jobs", icon: LucideBriefcase },
        { label: "Volunteer", path: "/experience/volunteer", icon: LucideHeartHandshake },
    ];

    readonly aboutLinks: AboutSubLink[] = [
        { label: "Certificates", path: "/certificates", icon: LucideAward },
        { label: "Education", path: "/education", icon: LucideGraduationCap },
        { label: "Hobbies", path: "/hobbies", icon: LucideHeart },
    ];

    // Flat link list for the fullscreen mobile menu — no nested dropdowns there.
    readonly mobileLinks: NavLink[] = [
        { label: "Home", path: "/", icon: LucideHouse },
        { label: "Projects", path: "/projects", icon: LucideFolderKanban },
        { label: "Jobs", path: "/experience/jobs", icon: LucideBriefcase },
        { label: "Volunteer", path: "/experience/volunteer", icon: LucideHeartHandshake },
        { label: "Skills", path: "/skills", icon: LucideLayers },
        { label: "Certificates", path: "/certificates", icon: LucideAward },
        { label: "Education", path: "/education", icon: LucideGraduationCap },
        { label: "Hobbies", path: "/hobbies", icon: LucideHeart },
    ];

    private readonly themeService = inject(ThemeService);
    private readonly authService = inject(AuthService);
    private readonly elementRef = inject(ElementRef);
    private readonly cvDownloadService = inject(CvDownloadService);

    private readonly isDownloadingCvSignal = signal(false);
    isDownloadingCv = this.isDownloadingCvSignal.asReadonly();

    private readonly isMenuOpenSignal = signal(false);
    isMenuOpen = this.isMenuOpenSignal.asReadonly();

    private readonly isExperienceOpenSignal = signal(false);
    isExperienceOpen = this.isExperienceOpenSignal.asReadonly();

    private readonly isAboutOpenSignal = signal(false);
    isAboutOpen = this.isAboutOpenSignal.asReadonly();

    isDarkMode = computed(() => this.themeService.theme() === 'dark');

    isAuthenticated = computed(() => this.authService.isAuthenticated());
    isAdmin = computed(() => this.authService.currentUser()?.role === 'ADMIN');

    toggleMenu(): void {
        this.isMenuOpenSignal.update((open) => !open);
        document.body.style.overflow = this.isMenuOpen() ? "hidden" : "";
    }

    closeMenu(): void {
        this.isMenuOpenSignal.set(false);
        this.isExperienceOpenSignal.set(false);
        this.isAboutOpenSignal.set(false);
        document.body.style.overflow = "";
    }

    toggleExperience(): void {
        this.isExperienceOpenSignal.update((open) => !open);
    }

    closeExperience(): void {
        this.isExperienceOpenSignal.set(false);
    }

    toggleAbout(): void {
        this.isAboutOpenSignal.update((open) => !open);
    }

    closeAbout(): void {
        this.isAboutOpenSignal.set(false);
    }

    @HostListener("document:click", ["$event"])
    onDocumentClick(event: MouseEvent): void {
        if (this.isExperienceOpen() && !this.elementRef.nativeElement.contains(event.target)) {
            this.closeExperience();
        }
        if (this.isAboutOpen() && !this.elementRef.nativeElement.contains(event.target)) {
            this.closeAbout();
        }
    }

    @HostListener("document:keydown.escape")
    onEscape(): void {
        if (this.isMenuOpen()) {
            this.closeMenu();
        }
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

    downloadCv(): void {
        if (this.isDownloadingCv()) {
            return;
        }
        this.isDownloadingCvSignal.set(true);
        this.cvDownloadService.generate().subscribe({
            next: () => this.isDownloadingCvSignal.set(false),
            error: () => this.isDownloadingCvSignal.set(false),
        });
    }
}
