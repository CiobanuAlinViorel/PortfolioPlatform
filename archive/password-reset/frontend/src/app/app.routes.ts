import { Routes } from '@angular/router';
import { RegisterPage } from '../features/auth/register/components/register.page';
import { LoginPage } from '../features/auth/login/components/login.page';
import { ForgotPasswordPage } from '../features/auth/forgot-password/components/forgot-password.page';
import { ResetPasswordPage } from '../features/auth/reset-password/components/reset-password.page';
import { HomePage } from '../features/home/components/home.component';
import { ProjectsListPage } from '../features/projects/components/projects-list.page';
import { ProjectDetailPage } from '../features/projects/components/project-detail.page';
import { SkillsListPage } from '../features/skills/components/skills-list.page';
import { CertificatesListPage } from '../features/certificates/components/certificates-list.page';
import { EducationListPage } from '../features/education/components/education-list.page';
import { HobbiesListPage } from '../features/hobbies/components/hobbies-list.page';
import { JobsListPage } from '../features/experience/jobs/components/jobs-list.page';
import { JobDetailPage } from '../features/experience/jobs/components/job-detail.page';
import { VolunteerListPage } from '../features/experience/volunteer/components/volunteer-list.page';
import { VolunteerDetailPage } from '../features/experience/volunteer/components/volunteer-detail.page';
import { AdminPageComponent } from '../features/admin/components/admin.page';
import { GeneralPage } from '../features/admin/features/general/components/general.page';
import { EducationPage } from '../features/admin/features/education/components/education.page';
import { JobExperiencesPage } from '../features/admin/features/job-experiences/components/job-experiences.page';
import { ProjectsPage } from '../features/admin/features/projects/components/projects.page';
import { SkillsPage } from '../features/admin/features/skills/components/skills.page';
import { CertificatesPage } from '../features/admin/features/certificates/components/certificates.page';
import { HobbiesPage } from '../features/admin/features/hobbies/components/hobbies.page';
import { VolunteerExperiencesPage } from '../features/admin/features/volunteer-experiences/components/volunteer-experiences.page';
import { adminGuard } from './admin.guard';
import { SeoData } from '../shared/services/seo.service';

const ADMIN_SEO_BASE: Pick<SeoData, 'noindex'> = { noindex: true };
const AUTH_SEO_BASE: Pick<SeoData, 'noindex'> = { noindex: true };

export const routes: Routes = [
    {
        path: '',
        component: HomePage,
        data: {
            seo: {
                title: 'Alin-Viorel Ciobanu — Portfolio',
                description: 'Portfolio of Alin-Viorel Ciobanu, a full-stack software developer. Explore projects, technical skills, work and volunteer experience, certificates, and education.',
                suffixTitle: false,
            } satisfies SeoData,
        },
    },
    {
        path: 'projects',
        component: ProjectsListPage,
        data: {
            seo: {
                title: 'Projects',
                description: 'Browse software projects built by Alin-Viorel Ciobanu, including full-stack applications, APIs, and tools, with details on features, tech stack, and outcomes.',
            } satisfies SeoData,
        },
    },
    {
        path: 'projects/:id',
        component: ProjectDetailPage,
        data: {
            seo: {
                title: 'Project Details',
                description: 'Detailed overview of a software project by Alin-Viorel Ciobanu, including features, challenges, tech stack, and results.',
            } satisfies SeoData,
        },
    },
    {
        path: 'skills',
        component: SkillsListPage,
        data: {
            seo: {
                title: 'Skills',
                description: 'Technical skills and technologies Alin-Viorel Ciobanu works with, including proficiency levels and ongoing learning progress.',
            } satisfies SeoData,
        },
    },
    {
        path: 'certificates',
        component: CertificatesListPage,
        data: {
            seo: {
                title: 'Certificates',
                description: 'Professional certifications and courses completed by Alin-Viorel Ciobanu across software development and related fields.',
            } satisfies SeoData,
        },
    },
    {
        path: 'education',
        component: EducationListPage,
        data: {
            seo: {
                title: 'Education',
                description: 'Academic background and coursework of Alin-Viorel Ciobanu, including degrees, institutions, and notable course projects.',
            } satisfies SeoData,
        },
    },
    {
        path: 'hobbies',
        component: HobbiesListPage,
        data: {
            seo: {
                title: 'Hobbies',
                description: 'Personal interests and hobbies of Alin-Viorel Ciobanu outside of software development.',
            } satisfies SeoData,
        },
    },
    {
        path: 'experience/jobs',
        component: JobsListPage,
        data: {
            seo: {
                title: 'Work Experience',
                description: 'Professional work experience of Alin-Viorel Ciobanu, including roles, companies, and projects delivered.',
            } satisfies SeoData,
        },
    },
    {
        path: 'experience/jobs/:id',
        component: JobDetailPage,
        data: {
            seo: {
                title: 'Work Experience Details',
                description: 'Details of a professional role held by Alin-Viorel Ciobanu, including responsibilities and projects delivered.',
            } satisfies SeoData,
        },
    },
    {
        path: 'experience/volunteer',
        component: VolunteerListPage,
        data: {
            seo: {
                title: 'Volunteer Experience',
                description: 'Volunteer work and community involvement of Alin-Viorel Ciobanu, including organizations, roles, and impact.',
            } satisfies SeoData,
        },
    },
    {
        path: 'experience/volunteer/:id',
        component: VolunteerDetailPage,
        data: {
            seo: {
                title: 'Volunteer Experience Details',
                description: 'Details of a volunteer role held by Alin-Viorel Ciobanu, including responsibilities, projects, and impact.',
            } satisfies SeoData,
        },
    },
    {
        path: 'register',
        component: RegisterPage,
        data: {
            seo: { title: 'Create Account', description: 'Create an account on Alin-Viorel Ciobanu’s portfolio site.', ...AUTH_SEO_BASE } satisfies SeoData,
        },
    },
    {
        path: 'login',
        component: LoginPage,
        data: {
            seo: { title: 'Sign In', description: 'Sign in to your account on Alin-Viorel Ciobanu’s portfolio site.', ...AUTH_SEO_BASE } satisfies SeoData,
        },
    },
    {
        path: 'forgot-password',
        component: ForgotPasswordPage,
        data: {
            seo: { title: 'Forgot Password', description: 'Reset the password for your account.', ...AUTH_SEO_BASE } satisfies SeoData,
        },
    },
    {
        path: 'reset-password',
        component: ResetPasswordPage,
        data: {
            seo: { title: 'Reset Password', description: 'Set a new password for your account.', ...AUTH_SEO_BASE } satisfies SeoData,
        },
    },
    {
        path: 'admin',
        canActivate: [adminGuard],
        children: [
            {
                path: '',
                component: AdminPageComponent,
                data: { seo: { title: 'Admin Dashboard', description: 'Admin dashboard for managing the portfolio site.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
            {
                path: 'general',
                component: GeneralPage,
                data: { seo: { title: 'Admin — General', description: 'Manage general profile and contact information.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
            {
                path: 'education',
                component: EducationPage,
                data: { seo: { title: 'Admin — Education', description: 'Manage education entries.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
            {
                path: 'job-experiences',
                component: JobExperiencesPage,
                data: { seo: { title: 'Admin — Work Experience', description: 'Manage work experience entries.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
            {
                path: 'projects',
                component: ProjectsPage,
                data: { seo: { title: 'Admin — Projects', description: 'Manage project entries.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
            {
                path: 'skills',
                component: SkillsPage,
                data: { seo: { title: 'Admin — Skills', description: 'Manage skill entries.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
            {
                path: 'certificates',
                component: CertificatesPage,
                data: { seo: { title: 'Admin — Certificates', description: 'Manage certificate entries.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
            {
                path: 'hobbies',
                component: HobbiesPage,
                data: { seo: { title: 'Admin — Hobbies', description: 'Manage hobby entries.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
            {
                path: 'volunteer-experiences',
                component: VolunteerExperiencesPage,
                data: { seo: { title: 'Admin — Volunteer Experience', description: 'Manage volunteer experience entries.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
        ]
    }
];
