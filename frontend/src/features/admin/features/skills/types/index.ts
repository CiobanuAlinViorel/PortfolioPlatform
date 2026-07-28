export type ProficiencyLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED" | "EXPERT";
export type LearningStatus = "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED" | "PAUSED";

export const PROFICIENCY_LEVELS: ProficiencyLevel[] = ["BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"];

export const LEARNING_STATUSES: LearningStatus[] = ["NOT_STARTED", "IN_PROGRESS", "COMPLETED", "PAUSED"];

export interface SkillCategory {
    id: number;
    name: string;
    description?: string;
    parentId?: number | null;
    sortOrder?: number;
}

export interface SkillListItem {
    id: number;
    name: string;
    categoryName?: string;
    proficiency: ProficiencyLevel;
    level?: number;
    yearsOfExperience?: number;
    description?: string;
    lastUsedDate?: string;
    hasCertification?: boolean;
    learning?: boolean;
}

export interface ProjectInSkill {
    id: number;
    title: string;
    description?: string;
    status: string;
    complexity: string;
    year?: number;
    githubUrl?: string;
    demoUrl?: string;
}

export interface LearningProgress {
    id?: number | null;
    name: string;
    status: LearningStatus;
    startDate?: string | null;
    completionDate?: string | null;
    progressPercentage?: number | null;
    timeSpentHours?: number | null;
    estimatedCompletion?: string;
    description?: string;
}

export interface SkillDetail {
    id: number;
    name: string;
    categoryName?: string;
    proficiency: ProficiencyLevel;
    level?: number;
    yearsOfExperience?: number;
    description?: string;
    lastUsedDate?: string;
    hasCertification?: boolean;
    learning?: boolean;
    tags: string[];
    projects: ProjectInSkill[];
    learningProgress: LearningProgress[];
}

export interface SkillTagWrite {
    id?: number | null;
    tagName: string;
}

export interface LearningProgressWrite {
    id?: number | null;
    name: string;
    status?: LearningStatus;
    startDate?: string | null;
    completionDate?: string | null;
    progressPercentage?: number | null;
    timeSpentHours?: number | null;
    estimatedCompletion?: string | null;
    description?: string | null;
}

export interface SkillWriteRequest {
    name?: string;
    categoryId?: number | null;
    categoryName?: string | null;
    proficiency?: ProficiencyLevel;
    level?: number | null;
    yearsOfExperience?: number | null;
    description?: string | null;
    lastUsedDate?: string | null;
    hasCertification?: boolean;
    learning?: boolean;
    sortOrder?: number | null;
    tags?: SkillTagWrite[];
    learningProgresses?: LearningProgressWrite[];
}
