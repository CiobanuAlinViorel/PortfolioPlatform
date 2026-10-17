import { Routes } from '@angular/router';
import { SeoData } from '../shared/services/seo.service';
import { adminGuard } from './admin.guard';

const ADMIN_SEO_BASE: Pick<SeoData, 'noindex'> = { noindex: true };
const AUTH_SEO_BASE: Pick<SeoData, 'noindex'> = { noindex: true };

export const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('../features/home/components/home.component').then(m => m.HomePage),
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
        loadComponent: () => import('../features/projects/components/projects-list.page').then(m => m.ProjectsListPage),
        data: {
            seo: {
                title: 'Projects',
                description: 'Browse software projects built by Alin-Viorel Ciobanu, including full-stack applications, APIs, and tools, with details on features, tech stack, and outcomes.',
            } satisfies SeoData,
        },
    },
    {
        path: 'projects/:id',
        loadComponent: () => import('../features/projects/components/project-detail.page').then(m => m.ProjectDetailPage),
        data: {
            seo: {
                title: 'Project Details',
                description: 'Detailed overview of a software project by Alin-Viorel Ciobanu, including features, challenges, tech stack, and results.',
            } satisfies SeoData,
        },
    },
    {
        path: 'skills',
        loadComponent: () => import('../features/skills/components/skills-list.page').then(m => m.SkillsListPage),
        data: {
            seo: {
                title: 'Skills',
                description: 'Technical skills and technologies Alin-Viorel Ciobanu works with, including proficiency levels and ongoing learning progress.',
            } satisfies SeoData,
        },
    },
    {
        path: 'certificates',
        loadComponent: () => import('../features/certificates/components/certificates-list.page').then(m => m.CertificatesListPage),
        data: {
            seo: {
                title: 'Certificates',
                description: 'Professional certifications and courses completed by Alin-Viorel Ciobanu across software development and related fields.',
            } satisfies SeoData,
        },
    },
    {
        path: 'education',
        loadComponent: () => import('../features/education/components/education-list.page').then(m => m.EducationListPage),
        data: {
            seo: {
                title: 'Education',
                description: 'Academic background and coursework of Alin-Viorel Ciobanu, including degrees, institutions, and notable course projects.',
            } satisfies SeoData,
        },
    },
    {
        path: 'hobbies',
        loadComponent: () => import('../features/hobbies/components/hobbies-list.page').then(m => m.HobbiesListPage),
        data: {
            seo: {
                title: 'Hobbies',
                description: 'Personal interests and hobbies of Alin-Viorel Ciobanu outside of software development.',
            } satisfies SeoData,
        },
    },
    {
        path: 'experience/jobs',
        loadComponent: () => import('../features/experience/jobs/components/jobs-list.page').then(m => m.JobsListPage),
        data: {
            seo: {
                title: 'Work Experience',
                description: 'Professional work experience of Alin-Viorel Ciobanu, including roles, companies, and projects delivered.',
            } satisfies SeoData,
        },
    },
    {
        path: 'experience/jobs/:id',
        loadComponent: () => import('../features/experience/jobs/components/job-detail.page').then(m => m.JobDetailPage),
        data: {
            seo: {
                title: 'Work Experience Details',
                description: 'Details of a professional role held by Alin-Viorel Ciobanu, including responsibilities and projects delivered.',
            } satisfies SeoData,
        },
    },
    {
        path: 'experience/volunteer',
        loadComponent: () => import('../features/experience/volunteer/components/volunteer-list.page').then(m => m.VolunteerListPage),
        data: {
            seo: {
                title: 'Volunteer Experience',
                description: 'Volunteer work and community involvement of Alin-Viorel Ciobanu, including organizations, roles, and impact.',
            } satisfies SeoData,
        },
    },
    {
        path: 'experience/volunteer/:id',
        loadComponent: () => import('../features/experience/volunteer/components/volunteer-detail.page').then(m => m.VolunteerDetailPage),
        data: {
            seo: {
                title: 'Volunteer Experience Details',
                description: 'Details of a volunteer role held by Alin-Viorel Ciobanu, including responsibilities, projects, and impact.',
            } satisfies SeoData,
        },
    },
    {
        path: 'register',
        loadComponent: () => import('../features/auth/register/components/register.page').then(m => m.RegisterPage),
        data: {
            seo: { title: 'Create Account', description: 'Create an account on Alin-Viorel Ciobanu\'s portfolio site.', ...AUTH_SEO_BASE } satisfies SeoData,
        },
    },
    {
        path: 'login',
        loadComponent: () => import('../features/auth/login/components/login.page').then(m => m.LoginPage),
        data: {
            seo: { title: 'Sign In', description: 'Sign in to your account on Alin-Viorel Ciobanu\'s portfolio site.', ...AUTH_SEO_BASE } satisfies SeoData,
        },
    },
    {
        path: 'admin',
        canActivate: [adminGuard],
        children: [
            {
                path: '',
                loadComponent: () => import('../features/admin/components/admin.page').then(m => m.AdminPageComponent),
                data: { seo: { title: 'Admin Dashboard', description: 'Admin dashboard for managing the portfolio site.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
            {
                path: 'general',
                loadComponent: () => import('../features/admin/features/general/components/general.page').then(m => m.GeneralPage),
                data: { seo: { title: 'Admin — General', description: 'Manage general profile and contact information.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
            {
                path: 'education',
                loadComponent: () => import('../features/admin/features/education/components/education.page').then(m => m.EducationPage),
                data: { seo: { title: 'Admin — Education', description: 'Manage education entries.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
            {
                path: 'job-experiences',
                loadComponent: () => import('../features/admin/features/job-experiences/components/job-experiences.page').then(m => m.JobExperiencesPage),
                data: { seo: { title: 'Admin — Work Experience', description: 'Manage work experience entries.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
            {
                path: 'projects',
                loadComponent: () => import('../features/admin/features/projects/components/projects.page').then(m => m.ProjectsPage),
                data: { seo: { title: 'Admin — Projects', description: 'Manage project entries.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
            {
                path: 'skills',
                loadComponent: () => import('../features/admin/features/skills/components/skills.page').then(m => m.SkillsPage),
                data: { seo: { title: 'Admin — Skills', description: 'Manage skill entries.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
            {
                path: 'certificates',
                loadComponent: () => import('../features/admin/features/certificates/components/certificates.page').then(m => m.CertificatesPage),
                data: { seo: { title: 'Admin — Certificates', description: 'Manage certificate entries.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
            {
                path: 'hobbies',
                loadComponent: () => import('../features/admin/features/hobbies/components/hobbies.page').then(m => m.HobbiesPage),
                data: { seo: { title: 'Admin — Hobbies', description: 'Manage hobby entries.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
            {
                path: 'volunteer-experiences',
                loadComponent: () => import('../features/admin/features/volunteer-experiences/components/volunteer-experiences.page').then(m => m.VolunteerExperiencesPage),
                data: { seo: { title: 'Admin — Volunteer Experience', description: 'Manage volunteer experience entries.', ...ADMIN_SEO_BASE } satisfies SeoData },
            },
        ]
    },
    {
        path: '**',
        loadComponent: () => import('../features/not-found/components/not-found.page').then(m => m.NotFoundPage),
        data: { seo: { title: 'Page not found', description: 'The page you are looking for does not exist.', noindex: true } satisfies SeoData }
    }
];
