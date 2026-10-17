import { Type } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { HomePage } from './home/components/home.component';
import { ProjectsListPage } from './projects/components/projects-list.page';
import { ProjectDetailPage } from './projects/components/project-detail.page';
import { SkillsListPage } from './skills/components/skills-list.page';
import { JobsListPage } from './experience/jobs/components/jobs-list.page';
import { JobDetailPage } from './experience/jobs/components/job-detail.page';
import { VolunteerListPage } from './experience/volunteer/components/volunteer-list.page';
import { VolunteerDetailPage } from './experience/volunteer/components/volunteer-detail.page';
import { CertificatesListPage } from './certificates/components/certificates-list.page';
import { EducationListPage } from './education/components/education-list.page';
import { HobbiesListPage } from './hobbies/components/hobbies-list.page';
import { NotFoundPage } from './not-found/components/not-found.page';
import { LoginPage } from './auth/login/components/login.page';
import { RegisterPage } from './auth/register/components/register.page';

const project = {
    id: 1, title: 'Portfolio Platform', description: 'A full-stack portfolio.', longDescription: 'Long text.\nSecond line.',
    status: 'PRODUCTION', complexity: 'ADVANCED', year: 2025, tags: ['angular', 'spring'], githubUrl: 'https://github.com/x', demoUrl: 'https://demo',
    categoryName: 'Web', sortOrder: 1, developmentTime: 120, imageUrl: 'https://img/1.png', skills: ['Angular', 'Java', 'PostgreSQL', 'Docker', 'Tailwind'],
    features: [{ title: 'Auth', description: 'JWT auth', implementationDate: '2025-01-01', developmentTimeHours: 10, sortOrder: 1 }],
    challenges: [{ title: 'Cold start', description: 'Render sleeps', solution: 'Banner', difficulty: 'MEDIUM' }],
    media: [{ imageUrl: 'https://img/1.png', title: 'Home', primary: true, sortOrder: 0 }, { imageUrl: 'https://img/2.png', primary: false, sortOrder: 1 }],
    metrics: { usersCount: 1500, linesOfCode: 42000, commitsCount: 310, testCoveragePercentage: 72 },
    achievements: [{ title: 'Shipped', description: 'Live', achievementType: 'MILESTONE', achievementDate: '2025-02-01', recognitionLevel: 'LOCAL', isFeatured: true, metricBefore: '2s', metricAfter: '200ms', proofUrl: 'https://proof' }],
};
const skills = [
    { id: 1, name: 'Angular', categoryName: 'Frontend', proficiency: 'EXPERT', level: 90, yearsOfExperience: 4, hasCertification: true, lastUsedDate: '2025-09-01' },
    { id: 2, name: 'Java', categoryName: 'Backend', proficiency: 'ADVANCED', level: 80, learning: true },
    { id: 3, name: 'Figma', proficiency: 'BEGINNER', level: 20 },
];
const jobs = [
    { id: 1, companyName: 'Acme', role: 'Engineer', startDate: '2023-01-01', endDate: null, projectsCount: 2 },
    { id: 2, companyName: 'Beta', role: 'Intern', startDate: '2021-06-01', endDate: '2022-06-01', projectsCount: 1 },
];
const jobDetail = { id: 1, companyName: 'Acme', role: 'Engineer', startDate: '2023-01-01', endDate: null, projects: [{ id: 1, title: 'P', status: 'PRODUCTION', tags: ['a'], year: 2024, githubUrl: 'https://g' }] };
const volunteers = [
    { id: 1, organization: 'Org', role: 'Mentor', type: 'NGO', location: 'Iasi', startDate: '2022-01-01', endDate: null, status: 'ONGOING', projectsCount: 1 },
    { id: 2, organization: 'Club', role: 'Member', type: 'CLUB', startDate: '2020-01-01', endDate: '2021-01-01', status: 'COMPLETED', projectsCount: 0 },
];
const volunteerDetail = {
    ...volunteers[0], description: 'Desc', impactDescription: 'Impact', website: 'https://org', hoursPerWeek: 4, totalHours: 300,
    responsibilities: [{ description: 'Teach', impactLevel: 'HIGH', sortOrder: 1 }],
    projects: [{ contributionPercentage: 40, project: { id: 1, title: 'P', status: 'PRODUCTION', tags: [] } }],
};
const certificates = [
    { id: 1, name: 'AWS CCP', provider: 'Amazon Web Services', issueDate: '2024-01-01', expiryDate: '2020-01-01', hasExpiry: true, verified: true, categoryName: 'Cloud', credentialId: 'ABC-123', certificateUrl: 'https://c', score: '900' },
    { id: 2, name: 'Angular', provider: 'Udemy', issueDate: '2023-01-01' },
];
const education = [
    { id: 1, level: 'BACHELOR', institution: 'UAIC', degree: 'BSc', fieldOfStudy: 'CS', location: 'Iasi', startDate: '2020-10-01', endDate: '2023-07-01', status: 'COMPLETED', gpa: '9.2', description: 'Desc' },
    { id: 2, level: 'MASTER', institution: 'UAIC', fieldOfStudy: 'SE', startDate: '2023-10-01', status: 'ONGOING' },
];
const hobbies = [
    { id: 1, name: 'Chess', category: 'GAMING', activityLevel: 'DAILY', complexityLevel: 'ADVANCED', yearsActive: 10, favoriteAspect: 'Tactics', description: 'Desc' },
    { id: 2, name: 'Cooking', category: 'COOKING', activityLevel: 'REGULAR' },
];
const profile = { profile: { id: 1, firstName: 'Ada', lastName: 'Lovelace', imageLink: 'https://img/a.png', description: 'x'.repeat(400) }, contact: { email: 'a@b.c', phone: '+40', github: 'https://g', linkedin: 'https://l', city: 'Iasi', country: 'RO' } };

function respond(url: string): object {
    if (url.includes('/profile/summary')) return profile;
    if (url.includes('/projects/public')) return [project, { ...project, id: 2, title: 'Second', categoryName: 'API', status: 'DEVELOPMENT', imageUrl: undefined, sortOrder: 2 }];
    if (/\/projects\/\d+$/.test(url)) return project;
    if (url.includes('/skills')) return skills;
    if (/\/experience\/jobs\/\d+$/.test(url)) return jobDetail;
    if (url.includes('/experience/jobs')) return jobs;
    if (/\/experience\/volunteers\/\d+$/.test(url)) return volunteerDetail;
    if (url.includes('/experience/volunteers')) return volunteers;
    if (url.includes('/certificates')) return certificates;
    if (url.includes('/education')) return education;
    if (url.includes('/hobbies')) return hobbies;
    return [];
}

async function render<T>(component: Type<T>, id = '1'): Promise<ComponentFixture<T>> {
    await TestBed.configureTestingModule({
        imports: [component],
        providers: [
            provideRouter([]),
            provideHttpClient(),
            provideHttpClientTesting(),
            { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id }), queryParamMap: convertToParamMap({}) } } },
        ],
    }).compileComponents();
    const fixture = TestBed.createComponent(component);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    for (let i = 0; i < 5; i++) {
        const pending = http.match(() => true);
        if (!pending.length) break;
        pending.forEach((req) => req.flush(respond(req.request.url)));
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    }
    return fixture;
}

const text = (f: ComponentFixture<unknown>) => (f.nativeElement as HTMLElement).textContent ?? '';
const html = (f: ComponentFixture<unknown>) => (f.nativeElement as HTMLElement).innerHTML;

describe('public pages (smoke)', () => {
    it('home renders hero, real stats, sections and no raw enums', async () => {
        const f = await render(HomePage);
        const t = text(f);
        expect(t).toContain('Ada Lovelace');
        expect(t).toContain('Selected projects');
        expect(t).toContain('Everything in one place');
        expect(t).toContain('Read more');
        expect(t).toContain('Years of experience');
        expect(t).not.toContain('PRODUCTION');
        expect(html(f)).not.toContain('Loading...');
    });

    it('projects list renders cards, filters and sort', async () => {
        const f = await render(ProjectsListPage);
        const t = text(f);
        expect(t).toContain('Portfolio Platform');
        expect(t).toContain('2 projects');
        expect(t).toContain('Production');
        expect(t).not.toContain('PRODUCTION');
        expect(html(f)).toContain('app-project-card');
        expect(html(f)).not.toContain('fixed bottom-6');
    });

    it('project detail renders tabs, gallery, metrics and achievements', async () => {
        const f = await render(ProjectDetailPage);
        const t = text(f);
        expect(t).toContain('Portfolio Platform');
        expect(t).toContain('Overview');
        expect(t).toContain('Features');
        expect(t).toContain('Metrics');
        expect(html(f)).toContain('role="tablist"');
        expect(html(f)).toContain('1 / 2');
    });

    it('skills group by category with progress bars', async () => {
        const f = await render(SkillsListPage);
        const t = text(f);
        expect(t).toContain('Frontend');
        expect(t).toContain('Other');
        expect(t).toContain('Expert');
        expect(html(f)).toContain('role="progressbar"');
    });

    it('jobs list renders a timeline with durations', async () => {
        const f = await render(JobsListPage);
        const t = text(f);
        expect(t).toContain('Acme');
        expect(t).toContain('Present');
        expect(t).toContain('Ongoing');
        expect(html(f)).toContain('<ol');
    });

    it('job detail renders compact project cards', async () => {
        const f = await render(JobDetailPage);
        expect(text(f)).toContain('Projects delivered');
        expect(html(f)).toContain('app-project-card');
    });

    it('volunteer list formats enums', async () => {
        const f = await render(VolunteerListPage);
        const t = text(f);
        expect(t).toContain('NGO');
        expect(t).toContain('Club');
        expect(t).not.toContain('CLUB');
        expect(t).toContain('Completed');
    });

    it('volunteer detail renders impact, responsibilities and contribution', async () => {
        const f = await render(VolunteerDetailPage);
        const t = text(f);
        expect(t).toContain('Impact');
        expect(t).toContain('High impact');
        expect(t).toContain('40%');
    });

    it('certificates render provider tiles, expired state and credential id', async () => {
        const f = await render(CertificatesListPage);
        const t = text(f);
        expect(t).toContain('AWS CCP');
        expect(t).toContain('Expired');
        expect(t).toContain('ABC-123');
        expect(t).toContain('Valid only');
    });

    it('education renders a timeline with formatted levels', async () => {
        const f = await render(EducationListPage);
        const t = text(f);
        expect(t).toContain("Bachelor's");
        expect(t).toContain('GPA');
        expect(t).not.toContain('BACHELOR');
    });

    it('hobbies render category icons and quotes', async () => {
        const f = await render(HobbiesListPage);
        const t = text(f);
        expect(t).toContain('Chess');
        expect(t).toContain('Gaming');
        expect(t).toContain('Tactics');
        expect(t).toContain('Daily');
    });

    it('login renders without Material', async () => {
        const login = await render(LoginPage);
        expect(text(login)).toContain('Welcome back');
        expect(html(login)).not.toContain('mat-');
    });

    it('register renders without Material', async () => {
        const register = await render(RegisterPage);
        expect(text(register)).toContain('Create an account');
        expect(html(register)).not.toContain('mat-');
    });

    it('404 renders', async () => {
        const f = await render(NotFoundPage);
        expect(text(f)).toContain('404');
    });
});
