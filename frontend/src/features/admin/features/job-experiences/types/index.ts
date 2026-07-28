export interface ProjectInJob {
    id: number;
    title: string;
    description?: string;
    status: string;
    year?: number;
    tags: string[];
    githubUrl?: string;
    demoUrl?: string;
}

export interface JobExperienceListItem {
    id: number;
    companyName: string;
    role: string;
    startDate: string;
    endDate?: string | null;
    projectsCount: number;
}

export interface JobExperienceDetail {
    id: number;
    companyName: string;
    role: string;
    startDate: string;
    endDate?: string | null;
    projects: ProjectInJob[];
}

export interface JobProjectWrite {
    id?: number | null;
    projectId: number;
}

export interface JobExperienceWriteRequest {
    companyName?: string;
    role?: string;
    startDate?: string;
    endDate?: string | null;
    projects?: JobProjectWrite[];
}
