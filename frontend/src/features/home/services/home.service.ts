import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable, forkJoin } from "rxjs";
import { map } from "rxjs/operators";
import { ProfileSummary } from "../../admin/features/general/types";
import { ProjectListItem } from "../../admin/features/projects/types";
import { SkillListItem } from "../../admin/features/skills/types";
import { JobExperienceListItem } from "../../admin/features/job-experiences/types";
import { environment } from "../../../environments/environment";
import { RequestCache } from "../../../shared/utils/cache";
import { ProjectsPublicService } from "../../projects/services/projects.service";
import { SkillsPublicService } from "../../skills/services/skills.service";
import { JobExperiencesPublicService } from "../../experience/jobs/services/job-experiences.service";

const API_URL = `${environment.api.baseUrl}/profile`;

export interface HomeSummary extends ProfileSummary {
    featuredProjects: ProjectListItem[];
    topSkills: SkillListItem[];
    currentJob?: JobExperienceListItem;
}

@Injectable({ providedIn: "root" })
export class HomeService {
    private readonly http = inject(HttpClient);
    private readonly cache = new RequestCache();
    private readonly projects = inject(ProjectsPublicService);
    private readonly skills = inject(SkillsPublicService);
    private readonly jobs = inject(JobExperiencesPublicService);

    /** Profile + contact info, cached for the session (used by header, footer and home). */
    getProfile(): Observable<ProfileSummary> {
        return this.cache.get(`${API_URL}/summary`, () => this.http.get<ProfileSummary>(`${API_URL}/summary`));
    }

    getSummary(): Observable<HomeSummary> {
        return forkJoin({
            profile: this.getProfile(),
            projects: this.projects.list(),
            skills: this.skills.list(),
            jobs: this.jobs.list(),
        }).pipe(
            map(({ profile, projects, skills, jobs }) => ({
                ...profile,
                featuredProjects: projects.slice(0, 3),
                topSkills: skills.slice(0, 10),
                currentJob: jobs.find(j => !j.endDate),
            }))
        );
    }
}
