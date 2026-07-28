export interface ContactInfo {
    email: string;
    phone?: string;
    github?: string;
    linkedin?: string;
    city?: string;
    country?: string;
}

export interface ProfileInfo {
    id: number;
    firstName: string;
    lastName: string;
    age?: number;
    imageLink?: string;
    description?: string;
}

export interface ProfileSummary {
    profile: ProfileInfo;
    contact: ContactInfo | null;
}

export interface CreateProfileRequest {
    firstName: string;
    lastName: string;
    age?: number | null;
    imageLink?: string | null;
    description?: string | null;
    contactInfo: ContactInfo;
}

export interface UpdateProfileRequest {
    firstName?: string;
    lastName?: string;
    age?: number | null;
    imageLink?: string | null;
    description?: string | null;
    contactInfo: ContactInfo;
}
