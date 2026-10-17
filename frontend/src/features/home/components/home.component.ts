import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
    LucideArrowDown,
    LucideArrowRight,
    LucideAward,
    LucideBriefcase,
    LucideDownload,
    LucideDynamicIcon,
    LucideFolderKanban,
    LucideGraduationCap,
    LucideHeart,
    LucideHeartHandshake,
    LucideIcon,
    LucideLayers,
    LucideMail,
    LucideMapPin,
    LucidePhone,
} from '@lucide/angular';
import { ContactInfo, ProfileInfo } from '../../admin/features/general/types';
import { HomeService } from '../services/home.service';
import { ProjectListItem } from '../../admin/features/projects/types';
import { SkillListItem } from '../../admin/features/skills/types';
import { JobExperienceListItem } from '../../admin/features/job-experiences/types';
import { VolunteerExperienceListItem } from '../../admin/features/volunteer-experiences/types';
import { Certificate } from '../../admin/features/certificates/types';
import { ProjectsPublicService } from '../../projects/services/projects.service';
import { SkillsPublicService } from '../../skills/services/skills.service';
import { JobExperiencesPublicService } from '../../experience/jobs/services/job-experiences.service';
import { VolunteerExperiencesPublicService } from '../../experience/volunteer/services/volunteer-experiences.service';
import { CertificatesPublicService } from '../../certificates/services/certificates.service';
import { EducationPublicService } from '../../education/services/education.service';
import { HobbiesPublicService } from '../../hobbies/services/hobbies.service';
import { CvDownloadService } from '../../../shared/services/cv-download.service';
import { SkeletonComponent } from '../../../shared/components/skeleton.component';
import { CardSkeletonComponent } from '../../../shared/components/skeleton.component';
import { BadgeComponent } from '../../../shared/components/badge.component';
import { StatComponent } from '../../../shared/components/stat.component';
import { ProjectCardComponent } from '../../../shared/components/project-card.component';
import { SectionComponent } from '../../../shared/components/section.component';
import { ButtonComponent } from '../../../shared/components/button.component';
import { AvatarComponent } from '../../../shared/components/avatar.component';
import { ErrorStateComponent } from '../../../shared/components/error-state.component';
import { BrandIconComponent } from '../../../shared/components/brand-icon.component';
import { RevealDirective } from '../../../shared/directives/reveal.directive';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';
import { duration } from '../../../shared/utils/duration';
import { yearsSince } from '../../../shared/utils/visual';

const PROFICIENCY_RANK: Record<string, number> = { EXPERT: 3, ADVANCED: 2, INTERMEDIATE: 1, BEGINNER: 0 };

interface ExploreTile {
    label: string;
    path: string;
    icon: LucideIcon;
    description: string;
    count: () => number | null;
    span?: string;
}

@Component({
    selector: 'home-page',
    imports: [
        RouterLink,
        LucideArrowDown,
        LucideArrowRight,
        LucideDownload,
        LucideDynamicIcon,
        LucideMail,
        LucideMapPin,
        LucidePhone,
        LucideBriefcase,
        LucideHeartHandshake,
        SkeletonComponent,
        CardSkeletonComponent,
        BadgeComponent,
        StatComponent,
        ProjectCardComponent,
        SectionComponent,
        ButtonComponent,
        AvatarComponent,
        ErrorStateComponent,
        BrandIconComponent,
        RevealDirective,
        EnumLabelPipe,
    ],
    templateUrl: '../ui/home.page.html',
})
export class HomePage implements OnInit {
    private readonly homeService = inject(HomeService);
    private readonly projectsService = inject(ProjectsPublicService);
    private readonly skillsService = inject(SkillsPublicService);
    private readonly jobsService = inject(JobExperiencesPublicService);
    private readonly volunteerService = inject(VolunteerExperiencesPublicService);
    private readonly certificatesService = inject(CertificatesPublicService);
    private readonly educationService = inject(EducationPublicService);
    private readonly hobbiesService = inject(HobbiesPublicService);
    private readonly cvService = inject(CvDownloadService);

    readonly profileLoading = signal(true);
    readonly profileError = signal(false);
    readonly profile = signal<ProfileInfo | null>(null);
    readonly contact = signal<ContactInfo | null>(null);
    readonly descriptionExpanded = signal(false);

    readonly projects = signal<ProjectListItem[] | null>(null);
    readonly skills = signal<SkillListItem[] | null>(null);
    readonly jobs = signal<JobExperienceListItem[] | null>(null);
    readonly volunteers = signal<VolunteerExperienceListItem[] | null>(null);
    readonly certificates = signal<Certificate[] | null>(null);
    readonly educationCount = signal<number | null>(null);
    readonly hobbiesCount = signal<number | null>(null);

    readonly isDownloadingCv = signal(false);

    readonly fullName = computed(() => {
        const p = this.profile();
        return p ? `${p.firstName} ${p.lastName}`.trim() : '';
    });

    readonly location = computed(() => {
        const c = this.contact();
        if (!c) return '';
        return c.city && c.country ? `${c.city}, ${c.country}` : c.city || c.country || '';
    });

    readonly descriptionIsLong = computed(() => (this.profile()?.description?.length ?? 0) > 280);

    readonly featuredProjects = computed(() =>
        [...(this.projects() ?? [])]
            .sort((a, b) => (a.sortOrder ?? 9999) - (b.sortOrder ?? 9999) || (b.year ?? 0) - (a.year ?? 0))
            .slice(0, 3),
    );

    readonly topSkills = computed(() =>
        [...(this.skills() ?? [])]
            .sort((a, b) => (b.level ?? -1) - (a.level ?? -1) || PROFICIENCY_RANK[b.proficiency] - PROFICIENCY_RANK[a.proficiency])
            .slice(0, 10),
    );

    readonly currentJob = computed(() => {
        const jobs = this.jobs() ?? [];
        return jobs.find((j) => !j.endDate) ?? [...jobs].sort((a, b) => b.startDate.localeCompare(a.startDate))[0] ?? null;
    });

    readonly latestVolunteer = computed(() => {
        const list = this.volunteers() ?? [];
        return [...list].sort((a, b) => b.startDate.localeCompare(a.startDate))[0] ?? null;
    });

    readonly yearsOfExperience = computed(() => {
        const jobs = this.jobs();
        if (!jobs?.length) return null;
        const earliest = jobs.map((j) => j.startDate).sort()[0];
        return yearsSince(earliest);
    });

    readonly stats = computed(() => {
        const items: Array<{ value: number; label: string; suffix?: string }> = [];
        const p = this.projects();
        const s = this.skills();
        const c = this.certificates();
        const y = this.yearsOfExperience();
        if (p) items.push({ value: p.length, label: 'Projects' });
        if (s) items.push({ value: s.length, label: 'Skills' });
        if (c) items.push({ value: c.length, label: 'Certificates' });
        if (y) items.push({ value: y, label: 'Years of experience', suffix: '+' });
        return items;
    });

    readonly statsLoading = computed(() => this.projects() === null && this.skills() === null && this.certificates() === null);

    readonly exploreTiles: ExploreTile[] = [
        { label: 'Projects', path: '/projects', icon: LucideFolderKanban, description: 'Full-stack apps, APIs and tools I have built.', count: () => this.projects()?.length ?? null, span: 'sm:col-span-2' },
        { label: 'Skills', path: '/skills', icon: LucideLayers, description: 'Technologies and proficiency levels.', count: () => this.skills()?.length ?? null },
        { label: 'Work', path: '/experience/jobs', icon: LucideBriefcase, description: 'Roles and the projects delivered in each.', count: () => this.jobs()?.length ?? null },
        { label: 'Volunteering', path: '/experience/volunteer', icon: LucideHeartHandshake, description: 'Community work and impact.', count: () => this.volunteers()?.length ?? null },
        { label: 'Certificates', path: '/certificates', icon: LucideAward, description: 'Courses and credentials.', count: () => this.certificates()?.length ?? null },
        { label: 'Education', path: '/education', icon: LucideGraduationCap, description: 'Degrees and coursework.', count: () => this.educationCount() },
        { label: 'Hobbies', path: '/hobbies', icon: LucideHeart, description: 'What I do when I am not coding.', count: () => this.hobbiesCount() },
    ];

    ngOnInit(): void {
        this.loadProfile();
        this.projectsService.list().subscribe({ next: (v) => this.projects.set(v), error: () => this.projects.set([]) });
        this.skillsService.list().subscribe({ next: (v) => this.skills.set(v), error: () => this.skills.set([]) });
        this.jobsService.list().subscribe({ next: (v) => this.jobs.set(v), error: () => this.jobs.set([]) });
        this.volunteerService.list().subscribe({ next: (v) => this.volunteers.set(v), error: () => this.volunteers.set([]) });
        this.certificatesService.list().subscribe({ next: (v) => this.certificates.set(v), error: () => this.certificates.set([]) });
        this.educationService.list().subscribe({ next: (v) => this.educationCount.set(v.length), error: () => this.educationCount.set(null) });
        this.hobbiesService.list().subscribe({ next: (v) => this.hobbiesCount.set(v.length), error: () => this.hobbiesCount.set(null) });
    }

    loadProfile(): void {
        this.profileLoading.set(true);
        this.profileError.set(false);
        this.homeService.getProfile().subscribe({
            next: (summary) => {
                this.profile.set(summary.profile);
                this.contact.set(summary.contact);
                this.profileLoading.set(false);
            },
            error: () => {
                this.profileError.set(true);
                this.profileLoading.set(false);
            },
        });
    }

    durationOf(start: string, end?: string | null): string {
        return duration(start, end);
    }

    downloadCv(): void {
        if (this.isDownloadingCv()) return;
        this.isDownloadingCv.set(true);
        this.cvService.generate().subscribe({
            next: () => this.isDownloadingCv.set(false),
            error: () => this.isDownloadingCv.set(false),
        });
    }
}
