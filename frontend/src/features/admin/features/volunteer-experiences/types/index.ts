export type VolunteerType = "ASSOCIATION" | "CLUB" | "COMMUNITY" | "NGO" | "CHARITY";
export type VolunteerStatus = "COMPLETED" | "ONGOING";
export type ImpactLevel = "LOW" | "MEDIUM" | "HIGH";

export interface ProjectInVolunteer {
    id: number;
    title: string;
    description?: string;
    status: string;
    year?: number;
    tags: string[];
    githubUrl?: string;
    demoUrl?: string;
}

export interface VolunteerResponsibility {
    id?: number | null;
    description: string;
    impactLevel: ImpactLevel;
    sortOrder?: number;
}

export interface VolunteerProject {
    id?: number | null;
    contributionPercentage: number;
    project: ProjectInVolunteer;
}

export interface VolunteerExperienceListItem {
    id: number;
    organization: string;
    role: string;
    type: VolunteerType;
    location?: string;
    startDate: string;
    endDate?: string | null;
    status: VolunteerStatus;
    projectsCount: number;
}

export interface VolunteerExperienceDetail {
    id: number;
    organization: string;
    role: string;
    type: VolunteerType;
    location?: string;
    startDate: string;
    endDate?: string | null;
    status: VolunteerStatus;
    description?: string;
    impactDescription?: string;
    website?: string;
    hoursPerWeek?: number | null;
    totalHours?: number | null;
    responsibilities: VolunteerResponsibility[];
    projects: VolunteerProject[];
}

export interface VolunteerResponsibilityWrite {
    id?: number | null;
    description: string;
    impactLevel: ImpactLevel;
    sortOrder?: number;
}

export interface VolunteerProjectWrite {
    id?: number | null;
    projectId: number;
    contributionPercentage: number;
}

export interface VolunteerExperienceWriteRequest {
    organization?: string;
    role?: string;
    type?: VolunteerType;
    location?: string;
    startDate?: string;
    endDate?: string | null;
    status?: VolunteerStatus;
    description?: string;
    impactDescription?: string;
    website?: string;
    hoursPerWeek?: number | null;
    totalHours?: number | null;
    responsibilities?: VolunteerResponsibilityWrite[];
    projects?: VolunteerProjectWrite[];
}
