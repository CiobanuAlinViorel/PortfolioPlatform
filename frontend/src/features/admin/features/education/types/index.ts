export type EducationLevel = "HIGH_SCHOOL" | "ASSOCIATE" | "BACHELOR" | "MASTER" | "PHD" | "CERTIFICATE" | "BOOTCAMP";
export type EducationStatus = "COMPLETED" | "ONGOING" | "DROPPED";
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

export const EDUCATION_LEVELS: EducationLevel[] = [
    "HIGH_SCHOOL",
    "ASSOCIATE",
    "BACHELOR",
    "MASTER",
    "PHD",
    "CERTIFICATE",
    "BOOTCAMP",
];

export const EDUCATION_STATUSES: EducationStatus[] = ["COMPLETED", "ONGOING", "DROPPED"];

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

export interface EducationListItem {
    id: number;
    level: EducationLevel;
    institution: string;
    degree?: string;
    fieldOfStudy: string;
    location?: string;
    startDate: string;
    endDate?: string;
    status: EducationStatus;
    gpa?: string;
    description?: string;
}

export interface ProjectInCourse {
    id: number;
    title: string;
    description?: string;
    status: string;
    year?: number;
    tags: string[];
    githubUrl?: string;
    demoUrl?: string;
}

export interface CourseProject {
    id: number;
    grade?: string;
    contributionPercentage?: number;
    project: ProjectInCourse;
}

export interface Course {
    id: number;
    title: string;
    description?: string;
    grade?: string;
    credits?: number;
    semester?: string;
    year?: number;
    relevant?: boolean;
    projects: CourseProject[];
}

export interface AchievementInEducation {
    id: number;
    achievementId: number;
    achievementTitle: string;
    courseId?: number;
    courseTitle?: string;
    topic: string;
    grade: number;
    maxGrade: number;
    credits: number;
    teacherOrSupervisor: string;
    institutionName: string;
    semester: number;
    academicYear: number;
}

export interface EducationDetail {
    id: number;
    level: EducationLevel;
    institution: string;
    degree?: string;
    fieldOfStudy: string;
    location?: string;
    startDate: string;
    endDate?: string;
    status: EducationStatus;
    gpa?: string;
    description?: string;
    courses: Course[];
    achievements: AchievementInEducation[];
}

// ── Write DTOs ─────────────────────────────────────────────────────────────

export interface CourseProjectWrite {
    id?: number | null;
    projectId: number;
    grade?: string | null;
    contributionPercentage?: number | null;
}

export interface CourseWrite {
    id?: number | null;
    title: string;
    description?: string | null;
    grade?: string | null;
    credits?: number | null;
    semester?: string | null;
    year?: number | null;
    relevant?: boolean;
    projects?: CourseProjectWrite[];
}

export interface EducationAchievementWrite {
    id?: number | null;
    achievementId?: number | null;

    achievementTitle?: string;
    achievementDescription?: string;
    achievementType?: AchievementType;
    achievementDate?: string;
    recognitionLevel?: RecognitionLevel;
    recognizedBy?: string | null;
    proofUrl?: string | null;
    isFeatured?: boolean;
    sortOrder?: number | null;

    courseId?: number | null;

    topic?: string;
    grade?: number;
    maxGrade?: number;
    credits?: number;
    teacherOrSupervisor?: string;
    institutionName?: string;
    semester?: number;
    academicYear?: number;
}

export interface EducationWriteRequest {
    level?: EducationLevel;
    institution?: string;
    degree?: string | null;
    fieldOfStudy?: string;
    location?: string | null;
    startDate?: string;
    endDate?: string | null;
    status?: EducationStatus;
    gpa?: string | null;
    description?: string | null;
    courses?: CourseWrite[];
    achievements?: EducationAchievementWrite[];
}

export interface ProjectOption {
    id: number;
    title: string;
}
