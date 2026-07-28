export type ProjectStatus = "PLANNING" | "DEVELOPMENT" | "TESTING" | "PRODUCTION" | "MAINTENANCE" | "ARCHIVED";
export type ComplexityLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
export type DifficultyLevel = "LOW" | "MEDIUM" | "HIGH";
export type ProficiencyLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED" | "EXPERT";
export type AchievementType =
    | "AWARD"
    | "CERTIFICATION"
    | "COMPLETION"
    | "MILESTONE"
    | "IMPACT"
    | "PUBLICATION"
    | "PRESENTATION"
    | "CONTRIBUTION"
    | "LEARNING";
export type RecognitionLevel = "LOCAL" | "REGIONAL" | "NATIONAL" | "INTERNATIONAL" | "INSTITUTIONAL";

export const PROJECT_STATUSES: ProjectStatus[] = [
    "PLANNING",
    "DEVELOPMENT",
    "TESTING",
    "PRODUCTION",
    "MAINTENANCE",
    "ARCHIVED",
];

export const COMPLEXITY_LEVELS: ComplexityLevel[] = ["BEGINNER", "INTERMEDIATE", "ADVANCED"];

export const DIFFICULTY_LEVELS: DifficultyLevel[] = ["LOW", "MEDIUM", "HIGH"];

export const PROFICIENCY_LEVELS: ProficiencyLevel[] = ["BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"];

export const ACHIEVEMENT_TYPES: AchievementType[] = [
    "AWARD",
    "CERTIFICATION",
    "COMPLETION",
    "MILESTONE",
    "IMPACT",
    "PUBLICATION",
    "PRESENTATION",
    "CONTRIBUTION",
    "LEARNING",
];

export const RECOGNITION_LEVELS: RecognitionLevel[] = ["LOCAL", "REGIONAL", "NATIONAL", "INTERNATIONAL", "INSTITUTIONAL"];

export interface ProjectCategory {
    id: number;
    name: string;
}

export interface ProjectListItem {
    id: number;
    title: string;
    description?: string;
    status: ProjectStatus;
    complexity: ComplexityLevel;
    year?: number;
    tags: string[];
    githubUrl?: string;
    demoUrl?: string;
    completionDate?: string;
    categoryName?: string;
    sortOrder?: number;
    developmentTime?: number;
    imageUrl?: string;
    skills: string[];
}

export interface ProjectFeature {
    id?: number | null;
    title: string;
    description?: string;
    implementationDate?: string | null;
    developmentTimeHours?: number | null;
    sortOrder?: number | null;
}

export interface ProjectChallenge {
    id?: number | null;
    title: string;
    description: string;
    solution?: string;
    difficulty: DifficultyLevel;
}

export interface ProjectMedia {
    id?: number | null;
    title?: string;
    description?: string;
    imageUrl?: string | null;
    altText?: string;
    sortOrder?: number | null;
    primary: boolean;
}

export interface ProjectMetrics {
    usersCount?: number | null;
    performanceScore?: string | null;
    codeQualityScore?: string | null;
    linesOfCode?: number | null;
    commitsCount?: number | null;
    testCoveragePercentage?: number | null;
}

export interface SkillInProject {
    id: number;
    name: string;
    proficiency: ProficiencyLevel;
    usage?: number | null;
}

export interface ProjectSkillWrite {
    skillName: string;
    proficiency?: ProficiencyLevel;
    level?: number | null;
    yearsOfExperience?: number | null;
    hasCertification?: boolean;
    learning?: boolean;
    usage?: number | null;
}

export interface ProjectAchievement {
    id?: number | null;
    title: string;
    description: string;
    achievementType: AchievementType;
    achievementDate: string;
    recognitionLevel: RecognitionLevel;
    recognizedBy?: string | null;
    proofUrl?: string | null;
    isFeatured?: boolean;
    impactSummary?: string | null;
    demoUrl?: string | null;
    metricBefore?: string | null;
    metricAfter?: string | null;
}

export interface SkillOption {
    id: number;
    name: string;
}

export interface ProjectDetail {
    id: number;
    title: string;
    description?: string;
    longDescription?: string;
    status: ProjectStatus;
    complexity: ComplexityLevel;
    year?: number;
    tags: string[];
    githubUrl?: string;
    demoUrl?: string;
    completionDate?: string;
    categoryName?: string;
    developmentTime?: number;
    features: ProjectFeature[];
    challenges: ProjectChallenge[];
    media: ProjectMedia[];
    metrics?: ProjectMetrics;
    skills: SkillInProject[];
    achievements: ProjectAchievement[];
}

export interface ProjectWriteRequest {
    title?: string;
    description?: string | null;
    longDescription?: string | null;
    status?: ProjectStatus;
    complexity?: ComplexityLevel;
    demoUrl?: string | null;
    githubUrl?: string | null;
    year?: number | null;
    completionDate?: string | null;
    developmentTime?: number | null;
    sortOrder?: number | null;
    tags?: string[];
    categoryName?: string | null;
    features?: ProjectFeature[];
    challenges?: ProjectChallenge[];
    media?: ProjectMedia[];
    metrics?: ProjectMetrics | null;
    skills?: ProjectSkillWrite[];
    achievements?: ProjectAchievement[];
}
