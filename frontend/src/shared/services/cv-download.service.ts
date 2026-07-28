import { Injectable, inject } from "@angular/core";
import { forkJoin, Observable } from "rxjs";
import { map } from "rxjs/operators";
import { jsPDF } from "jspdf";
import { HomeService } from "../../features/home/services/home.service";
import { SkillsPublicService } from "../../features/skills/services/skills.service";
import { ProjectsPublicService } from "../../features/projects/services/projects.service";
import { JobExperiencesPublicService } from "../../features/experience/jobs/services/job-experiences.service";
import { VolunteerExperiencesPublicService } from "../../features/experience/volunteer/services/volunteer-experiences.service";
import { EducationPublicService } from "../../features/education/services/education.service";
import { HobbiesPublicService } from "../../features/hobbies/services/hobbies.service";
import { ProfileSummary } from "../../features/admin/features/general/types";
import { SkillListItem } from "../../features/admin/features/skills/types";
import { ProjectListItem } from "../../features/admin/features/projects/types";
import { JobExperienceListItem } from "../../features/admin/features/job-experiences/types";
import { VolunteerExperienceListItem } from "../../features/admin/features/volunteer-experiences/types";
import { EducationListItem } from "../../features/admin/features/education/types";
import { HobbyListItem } from "../../features/admin/features/hobbies/types";

interface CvData {
    summary: ProfileSummary;
    skills: SkillListItem[];
    projects: ProjectListItem[];
    jobs: JobExperienceListItem[];
    volunteers: VolunteerExperienceListItem[];
    education: EducationListItem[];
    hobbies: HobbyListItem[];
}

const MAIN_PROJECTS_LIMIT = 5;
const PAGE_WIDTH = 210;
const MARGIN = 15;
const CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2;
const PAGE_HEIGHT = 297;

@Injectable({ providedIn: "root" })
export class CvDownloadService {
    private readonly homeService = inject(HomeService);
    private readonly skillsService = inject(SkillsPublicService);
    private readonly projectsService = inject(ProjectsPublicService);
    private readonly jobsService = inject(JobExperiencesPublicService);
    private readonly volunteerService = inject(VolunteerExperiencesPublicService);
    private readonly educationService = inject(EducationPublicService);
    private readonly hobbiesService = inject(HobbiesPublicService);

    private fetchData(): Observable<CvData> {
        return forkJoin({
            summary: this.homeService.getSummary(),
            skills: this.skillsService.list(),
            projects: this.projectsService.list(),
            jobs: this.jobsService.list(),
            volunteers: this.volunteerService.list(),
            education: this.educationService.list(),
            hobbies: this.hobbiesService.list(),
        });
    }

    generate(): Observable<void> {
        return this.fetchData().pipe(map((data) => this.buildPdf(data)));
    }

    private buildPdf(data: CvData): void {
        const doc = new jsPDF({ unit: "mm", format: "a4" });
        let y = MARGIN;

        y = this.renderHeader(doc, data.summary, y);
        y = this.renderSection(doc, y, "Skills", (y) => this.renderSkills(doc, y, data.skills));
        y = this.renderSection(doc, y, "Main Projects", (y) => this.renderProjects(doc, y, data.projects));
        y = this.renderSection(doc, y, "Work Experience", (y) => this.renderJobs(doc, y, data.jobs));
        y = this.renderSection(doc, y, "Volunteer Experience", (y) => this.renderVolunteers(doc, y, data.volunteers));
        y = this.renderSection(doc, y, "Education", (y) => this.renderEducation(doc, y, data.education));
        y = this.renderSection(doc, y, "Hobbies & Interests", (y) => this.renderHobbies(doc, y, data.hobbies));

        const fileName = `CV_${data.summary.profile.firstName}_${data.summary.profile.lastName}.pdf`.replace(/\s+/g, "_");
        doc.save(fileName);
    }

    private ensureSpace(doc: jsPDF, y: number, needed: number): number {
        if (y + needed > PAGE_HEIGHT - MARGIN) {
            doc.addPage();
            return MARGIN;
        }
        return y;
    }

    private renderHeader(doc: jsPDF, summary: ProfileSummary, y: number): number {
        const { profile, contact } = summary;
        doc.setFont("helvetica", "bold");
        doc.setFontSize(22);
        doc.text(`${profile.firstName} ${profile.lastName}`, MARGIN, y);
        y += 8;

        const contactLine = [contact?.email, contact?.phone, contact?.city && contact?.country ? `${contact.city}, ${contact.country}` : contact?.city || contact?.country]
            .filter(Boolean)
            .join("  |  ");
        const linksLine = [contact?.github, contact?.linkedin].filter(Boolean).join("  |  ");

        doc.setFont("helvetica", "normal");
        doc.setFontSize(10);
        if (contactLine) {
            doc.text(contactLine, MARGIN, y);
            y += 5;
        }
        if (linksLine) {
            doc.text(linksLine, MARGIN, y);
            y += 5;
        }

        if (profile.description) {
            y += 2;
            doc.setFontSize(10);
            const lines = doc.splitTextToSize(profile.description, CONTENT_WIDTH);
            doc.text(lines, MARGIN, y);
            y += lines.length * 5;
        }

        y += 3;
        doc.setDrawColor(180);
        doc.line(MARGIN, y, PAGE_WIDTH - MARGIN, y);
        y += 8;
        return y;
    }

    private renderSection(doc: jsPDF, y: number, title: string, render: (y: number) => number): number {
        y = this.ensureSpace(doc, y, 16);
        doc.setFont("helvetica", "bold");
        doc.setFontSize(13);
        doc.text(title, MARGIN, y);
        y += 6;
        doc.setDrawColor(210);
        doc.line(MARGIN, y - 3, PAGE_WIDTH - MARGIN, y - 3);

        doc.setFont("helvetica", "normal");
        doc.setFontSize(10);
        y = render(y);
        y += 6;
        return y;
    }

    private renderSkills(doc: jsPDF, y: number, skills: SkillListItem[]): number {
        if (skills.length === 0) {
            doc.text("No skills listed.", MARGIN, y);
            return y + 5;
        }
        const grouped = new Map<string, string[]>();
        for (const skill of skills) {
            const category = skill.categoryName ?? "Other";
            const label = skill.yearsOfExperience ? `${skill.name} (${skill.yearsOfExperience}y, ${skill.proficiency})` : `${skill.name} (${skill.proficiency})`;
            grouped.set(category, [...(grouped.get(category) ?? []), label]);
        }
        for (const [category, items] of grouped) {
            y = this.ensureSpace(doc, y, 10);
            doc.setFont("helvetica", "bold");
            doc.text(`${category}:`, MARGIN, y);
            doc.setFont("helvetica", "normal");
            const lines = doc.splitTextToSize(items.join(", "), CONTENT_WIDTH - 30);
            doc.text(lines, MARGIN + 30, y);
            y += Math.max(5, lines.length * 5);
        }
        return y;
    }

    private renderProjects(doc: jsPDF, y: number, projects: ProjectListItem[]): number {
        const main = [...projects]
            .sort((a, b) => (a.sortOrder ?? 999) - (b.sortOrder ?? 999))
            .slice(0, MAIN_PROJECTS_LIMIT);

        if (main.length === 0) {
            doc.text("No projects listed.", MARGIN, y);
            return y + 5;
        }

        for (const project of main) {
            y = this.ensureSpace(doc, y, 16);
            doc.setFont("helvetica", "bold");
            const yearLabel = project.year ? ` (${project.year})` : "";
            doc.text(`${project.title}${yearLabel}`, MARGIN, y);
            y += 5;

            if (project.description) {
                doc.setFont("helvetica", "normal");
                const lines = doc.splitTextToSize(project.description, CONTENT_WIDTH);
                doc.text(lines, MARGIN, y);
                y += lines.length * 5;
            }

            if (project.skills.length > 0) {
                doc.setFont("helvetica", "italic");
                const lines = doc.splitTextToSize(`Tech: ${project.skills.join(", ")}`, CONTENT_WIDTH);
                doc.text(lines, MARGIN, y);
                y += lines.length * 5;
            }
            y += 3;
        }
        return y;
    }

    private renderJobs(doc: jsPDF, y: number, jobs: JobExperienceListItem[]): number {
        if (jobs.length === 0) {
            doc.text("No work experience listed.", MARGIN, y);
            return y + 5;
        }
        for (const job of jobs) {
            y = this.ensureSpace(doc, y, 12);
            doc.setFont("helvetica", "bold");
            doc.text(`${job.role} - ${job.companyName}`, MARGIN, y);
            doc.setFont("helvetica", "normal");
            const period = `${this.formatDate(job.startDate)} - ${job.endDate ? this.formatDate(job.endDate) : "Present"}`;
            doc.text(period, PAGE_WIDTH - MARGIN, y, { align: "right" });
            y += 6;
        }
        return y;
    }

    private renderVolunteers(doc: jsPDF, y: number, volunteers: VolunteerExperienceListItem[]): number {
        if (volunteers.length === 0) {
            doc.text("No volunteer experience listed.", MARGIN, y);
            return y + 5;
        }
        for (const v of volunteers) {
            y = this.ensureSpace(doc, y, 12);
            doc.setFont("helvetica", "bold");
            doc.text(`${v.role} - ${v.organization}`, MARGIN, y);
            doc.setFont("helvetica", "normal");
            const period = `${this.formatDate(v.startDate)} - ${v.endDate ? this.formatDate(v.endDate) : "Present"}`;
            doc.text(period, PAGE_WIDTH - MARGIN, y, { align: "right" });
            y += 6;
        }
        return y;
    }

    private renderEducation(doc: jsPDF, y: number, education: EducationListItem[]): number {
        if (education.length === 0) {
            doc.text("No education listed.", MARGIN, y);
            return y + 5;
        }
        for (const edu of education) {
            y = this.ensureSpace(doc, y, 12);
            doc.setFont("helvetica", "bold");
            const degreeLabel = edu.degree ? `${edu.degree}, ` : "";
            doc.text(`${degreeLabel}${edu.fieldOfStudy} - ${edu.institution}`, MARGIN, y);
            doc.setFont("helvetica", "normal");
            const period = `${this.formatDate(edu.startDate)} - ${edu.endDate ? this.formatDate(edu.endDate) : "Present"}`;
            doc.text(period, PAGE_WIDTH - MARGIN, y, { align: "right" });
            y += 6;
        }
        return y;
    }

    private renderHobbies(doc: jsPDF, y: number, hobbies: HobbyListItem[]): number {
        if (hobbies.length === 0) {
            doc.text("No hobbies listed.", MARGIN, y);
            return y + 5;
        }
        const text = hobbies.map((h) => h.name).join(", ");
        const lines = doc.splitTextToSize(text, CONTENT_WIDTH);
        doc.text(lines, MARGIN, y);
        return y + lines.length * 5;
    }

    private formatDate(value: string): string {
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return value;
        }
        return date.toLocaleDateString("en-US", { month: "short", year: "numeric" });
    }
}
