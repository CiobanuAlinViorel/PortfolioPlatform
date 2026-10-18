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

export interface CvData {
    summary: ProfileSummary;
    skills: SkillListItem[];
    projects: ProjectListItem[];
    jobs: JobExperienceListItem[];
    volunteers: VolunteerExperienceListItem[];
    education: EducationListItem[];
    hobbies: HobbyListItem[];
}

type FontStyle = "normal" | "bold" | "italic" | "bolditalic";

interface TextStyle {
    size: number;
    style?: FontStyle;
    align?: "left" | "right";
    color?: number;
}

interface Column {
    x: number;
    width: number;
    y: number;
    page: number;
}

const MAIN_PROJECTS_LIMIT = 5;
export const PAGE = { width: 210, height: 297, margin: 15 } as const;
const CONTENT_WIDTH = PAGE.width - PAGE.margin * 2;
const SIDEBAR_WIDTH = 60;
const GUTTER = 8;
const SIDEBAR_BAND_WIDTH = PAGE.margin + SIDEBAR_WIDTH + GUTTER / 2;
const MAIN_X = PAGE.margin + SIDEBAR_WIDTH + GUTTER;
const MAIN_WIDTH = PAGE.width - PAGE.margin - MAIN_X;

export const FONT_FAMILY = "times";
const FONT = { name: 22, h2: 12, body: 10, small: 9 } as const;
const PT_TO_MM = 0.3528;
const LINE_HEIGHT_FACTOR = 1.25;
const TEXT_COLOR = 30;
const MUTED_COLOR = 90;
const RULE_COLOR = 190;
const BAND_COLOR: [number, number, number] = [243, 244, 246];

const CHAR_MAP: Record<string, string> = {
    "\u2013": "-",
    "\u2014": "-",
    "\u2018": "'",
    "\u2019": "'",
    "\u201c": '"',
    "\u201d": '"',
    "\u2026": "...",
    "\u2022": "-",
    "\u00a0": " ",
};

export const lineHeight = (pt: number): number => pt * PT_TO_MM * LINE_HEIGHT_FACTOR;

export function sanitize(text: string): string {
    return text
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .replace(/[^\u0000-\u00ff]/g, (c) => CHAR_MAP[c] ?? "");
}

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
        return this.fetchData().pipe(
            map((data) => {
                const fileName = sanitize(`CV_${data.summary.profile.firstName}_${data.summary.profile.lastName}.pdf`).replace(/\s+/g, "_");
                this.buildDocument(data).save(fileName);
            }),
        );
    }

    createDocument(): jsPDF {
        return new jsPDF({ unit: "mm", format: "a4" });
    }

    buildDocument(data: CvData): jsPDF {
        const doc = this.createDocument();
        doc.setLineHeightFactor(LINE_HEIGHT_FACTOR);

        const header: Column = { x: PAGE.margin, width: CONTENT_WIDTH, y: PAGE.margin, page: 1 };
        this.renderHeader(doc, header, data.summary);
        const bodyTop = header.y;
        this.drawSidebarBand(doc, bodyTop);

        const sidebar: Column = { x: PAGE.margin, width: SIDEBAR_WIDTH, y: bodyTop, page: 1 };
        this.renderSection(doc, sidebar, "Skills", () => this.renderSkills(doc, sidebar, data.skills));
        this.renderSection(doc, sidebar, "Hobbies & Interests", () => this.renderHobbies(doc, sidebar, data.hobbies));

        const main: Column = { x: MAIN_X, width: MAIN_WIDTH, y: bodyTop, page: 1 };
        this.renderSection(doc, main, "Work Experience", () => this.renderDatedEntries(doc, main, data.jobs.map((j) => ({ title: `${j.role} - ${j.companyName}`, start: j.startDate, end: j.endDate })), "No work experience listed."));
        this.renderSection(doc, main, "Volunteer Experience", () => this.renderDatedEntries(doc, main, data.volunteers.map((v) => ({ title: `${v.role} - ${v.organization}`, start: v.startDate, end: v.endDate })), "No volunteer experience listed."));
        this.renderSection(doc, main, "Education", () => this.renderDatedEntries(doc, main, data.education.map((e) => ({ title: `${e.degree ? `${e.degree}, ` : ""}${e.fieldOfStudy} - ${e.institution}`, start: e.startDate, end: e.endDate })), "No education listed."));
        this.renderSection(doc, main, "Main Projects", () => this.renderProjects(doc, main, data.projects));

        return doc;
    }

    // --- layout primitives -------------------------------------------------

    private ensureSpace(doc: jsPDF, col: Column, needed: number): void {
        if (col.y + needed > PAGE.height - PAGE.margin) {
            col.page += 1;
            col.y = PAGE.margin;
            if (col.page > doc.getNumberOfPages()) {
                doc.addPage();
                this.drawSidebarBand(doc, 0);
            }
        }
        doc.setPage(col.page);
    }

    private drawSidebarBand(doc: jsPDF, top: number): void {
        doc.setFillColor(...BAND_COLOR);
        doc.rect(0, top, SIDEBAR_BAND_WIDTH, PAGE.height - top, "F");
    }

    private applyStyle(doc: jsPDF, style: TextStyle): void {
        doc.setFont(FONT_FAMILY, style.style ?? "normal");
        doc.setFontSize(style.size);
        doc.setTextColor(style.color ?? TEXT_COLOR);
    }

    private writeText(doc: jsPDF, col: Column, text: string, style: TextStyle): void {
        this.applyStyle(doc, style);
        const lines: string[] = doc.splitTextToSize(sanitize(text), col.width);
        const h = lineHeight(style.size);
        const x = style.align === "right" ? col.x + col.width : col.x;
        for (const line of lines) {
            this.ensureSpace(doc, col, h);
            doc.text(line, x, col.y, { baseline: "top", align: style.align ?? "left" });
            col.y += h;
        }
    }

    private drawRule(doc: jsPDF, col: Column): void {
        doc.setPage(col.page);
        doc.setDrawColor(RULE_COLOR);
        doc.setLineWidth(0.3);
        doc.line(col.x, col.y, col.x + col.width, col.y);
    }

    private gap(col: Column, lines: number, size: number = FONT.body): void {
        col.y += lineHeight(size) * lines;
    }

    // --- sections ----------------------------------------------------------

    private renderHeader(doc: jsPDF, col: Column, summary: ProfileSummary): void {
        const { profile, contact } = summary;
        this.writeText(doc, col, `${profile.firstName} ${profile.lastName}`, { size: FONT.name, style: "bold" });
        this.gap(col, 0.3);

        const location = contact?.city && contact?.country ? `${contact.city}, ${contact.country}` : contact?.city || contact?.country;
        const contactLine = [contact?.email, contact?.phone, location].filter(Boolean).join("  |  ");
        const linksLine = [contact?.github, contact?.linkedin].filter(Boolean).join("  |  ");
        if (contactLine) this.writeText(doc, col, contactLine, { size: FONT.small, color: MUTED_COLOR });
        if (linksLine) this.writeText(doc, col, linksLine, { size: FONT.small, color: MUTED_COLOR });

        if (profile.description) {
            this.gap(col, 0.8);
            this.writeText(doc, col, profile.description, { size: FONT.body });
        }

        this.gap(col, 1);
        this.drawRule(doc, col);
        this.gap(col, 1.5);
    }

    private renderSection(doc: jsPDF, col: Column, title: string, render: () => void): void {
        this.ensureSpace(doc, col, lineHeight(FONT.h2) + lineHeight(FONT.body) * 3.5);
        this.writeText(doc, col, title.toUpperCase(), { size: FONT.h2, style: "bold" });
        this.gap(col, 0.4);
        this.drawRule(doc, col);
        this.gap(col, 1);
        render();
        this.gap(col, 1.5);
    }

    private renderSkills(doc: jsPDF, col: Column, skills: SkillListItem[]): void {
        if (skills.length === 0) {
            this.writeText(doc, col, "No skills listed.", { size: FONT.body });
            return;
        }
        const grouped = new Map<string, string[]>();
        for (const skill of skills) {
            const category = skill.categoryName ?? "Other";
            const label = skill.yearsOfExperience ? `${skill.name} (${skill.yearsOfExperience}y, ${skill.proficiency})` : `${skill.name} (${skill.proficiency})`;
            grouped.set(category, [...(grouped.get(category) ?? []), label]);
        }
        let first = true;
        for (const [category, items] of grouped) {
            if (!first) this.gap(col, 0.6);
            first = false;
            this.ensureSpace(doc, col, lineHeight(FONT.body) + lineHeight(FONT.small));
            this.writeText(doc, col, category, { size: FONT.body, style: "bold" });
            this.writeText(doc, col, items.join(", "), { size: FONT.small });
        }
    }

    private renderHobbies(doc: jsPDF, col: Column, hobbies: HobbyListItem[]): void {
        if (hobbies.length === 0) {
            this.writeText(doc, col, "No hobbies listed.", { size: FONT.body });
            return;
        }
        this.writeText(doc, col, hobbies.map((h) => h.name).join(", "), { size: FONT.small });
    }

    private renderDatedEntries(doc: jsPDF, col: Column, entries: { title: string; start: string; end?: string | null }[], emptyLabel: string): void {
        if (entries.length === 0) {
            this.writeText(doc, col, emptyLabel, { size: FONT.body });
            return;
        }
        entries.forEach((entry, index) => {
            if (index > 0) this.gap(col, 0.6);
            const period = `${this.formatDate(entry.start)} - ${entry.end ? this.formatDate(entry.end) : "Present"}`;
            this.writeDatedLine(doc, col, entry.title, period);
        });
    }

    private writeDatedLine(doc: jsPDF, col: Column, title: string, period: string): void {
        const titleStyle: TextStyle = { size: FONT.body, style: "bold" };
        const periodStyle: TextStyle = { size: FONT.small, color: MUTED_COLOR, align: "right" };
        this.applyStyle(doc, titleStyle);
        const titleWidth = doc.getTextWidth(sanitize(title));
        this.applyStyle(doc, periodStyle);
        const periodWidth = doc.getTextWidth(period);

        if (titleWidth + periodWidth + 4 <= col.width) {
            const h = lineHeight(FONT.body);
            this.ensureSpace(doc, col, h);
            this.applyStyle(doc, titleStyle);
            doc.text(sanitize(title), col.x, col.y, { baseline: "top" });
            this.applyStyle(doc, periodStyle);
            doc.text(period, col.x + col.width, col.y, { baseline: "top", align: "right" });
            col.y += h;
            return;
        }
        this.writeText(doc, col, title, titleStyle);
        this.writeText(doc, col, period, periodStyle);
    }

    private renderProjects(doc: jsPDF, col: Column, projects: ProjectListItem[]): void {
        const main = [...projects].sort((a, b) => (a.sortOrder ?? 999) - (b.sortOrder ?? 999)).slice(0, MAIN_PROJECTS_LIMIT);
        if (main.length === 0) {
            this.writeText(doc, col, "No projects listed.", { size: FONT.body });
            return;
        }
        main.forEach((project, index) => {
            if (index > 0) this.gap(col, 0.8);
            this.ensureSpace(doc, col, lineHeight(FONT.body) * 2.5);
            this.writeText(doc, col, project.year ? `${project.title} (${project.year})` : project.title, { size: FONT.body, style: "bold" });
            if (project.description) this.writeText(doc, col, project.description, { size: FONT.body });
            if (project.skills.length > 0) this.writeText(doc, col, `Tech: ${project.skills.join(", ")}`, { size: FONT.small, style: "italic", color: MUTED_COLOR });
        });
    }

    private formatDate(value: string): string {
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return value;
        }
        return date.toLocaleDateString("en-US", { month: "short", year: "numeric" });
    }
}
