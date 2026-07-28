export type HobbyCategory =
    | "LEARNING"
    | "SPORTS"
    | "CREATIVE"
    | "SOCIAL"
    | "TECHNOLOGY"
    | "MUSIC"
    | "TRAVEL"
    | "COOKING"
    | "GARDENING"
    | "READING"
    | "GAMING";
export type ActivityLevel = "OCCASIONAL" | "REGULAR" | "FREQUENT" | "DAILY";
export type ComplexityLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
export type ImpactLevel = "LOW" | "MEDIUM" | "HIGH";

export const HOBBY_CATEGORIES: HobbyCategory[] = [
    "LEARNING",
    "SPORTS",
    "CREATIVE",
    "SOCIAL",
    "TECHNOLOGY",
    "MUSIC",
    "TRAVEL",
    "COOKING",
    "GARDENING",
    "READING",
    "GAMING",
];

export const ACTIVITY_LEVELS: ActivityLevel[] = ["OCCASIONAL", "REGULAR", "FREQUENT", "DAILY"];

export const COMPLEXITY_LEVELS: ComplexityLevel[] = ["BEGINNER", "INTERMEDIATE", "ADVANCED"];

export const IMPACT_LEVELS: ImpactLevel[] = ["LOW", "MEDIUM", "HIGH"];

export interface HobbyListItem {
    id: number;
    name: string;
    description?: string;
    category: HobbyCategory;
    activityLevel?: ActivityLevel;
    complexityLevel?: ComplexityLevel;
    impactOnWork?: ImpactLevel;
    yearsActive?: number;
    whyInterested?: string;
    favoriteAspect?: string;
}

export interface SkillInHobby {
    skillId: number;
    skillName: string;
    usagePercentage: number;
}

export interface HobbyDetail {
    id: number;
    name: string;
    description?: string;
    category: HobbyCategory;
    activityLevel?: ActivityLevel;
    complexityLevel?: ComplexityLevel;
    impactOnWork?: ImpactLevel;
    yearsActive?: number;
    whyInterested?: string;
    favoriteAspect?: string;
    skills: SkillInHobby[];
}

export interface HobbySkillWrite {
    id?: number | null;
    skillId: number;
    usagePercentage: number;
}

export interface HobbyWriteRequest {
    name?: string;
    description?: string | null;
    category?: HobbyCategory;
    activityLevel?: ActivityLevel;
    complexityLevel?: ComplexityLevel;
    impactOnWork?: ImpactLevel;
    yearsActive?: number | null;
    whyInterested?: string | null;
    favoriteAspect?: string | null;
    skills?: HobbySkillWrite[];
}

export interface SkillOption {
    id: number;
    name: string;
}
