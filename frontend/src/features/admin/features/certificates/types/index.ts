export interface CertificateCategory {
    id: number;
    name: string;
    description?: string;
    industry?: string;
    sortOrder?: number;
}

export interface Certificate {
    id: number;
    name: string;
    provider: string;
    credentialId?: string;
    certificateUrl?: string;
    issueDate: string;
    expiryDate?: string;
    hasExpiry?: boolean;
    description?: string;
    score?: string;
    relevanceScore?: number;
    verified?: boolean;
    categoryId?: number;
    categoryName?: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface CertificateWriteRequest {
    name?: string;
    provider?: string;
    credentialId?: string | null;
    certificateUrl?: string | null;
    issueDate?: string;
    expiryDate?: string | null;
    hasExpiry?: boolean;
    description?: string | null;
    score?: string | null;
    relevanceScore?: number | null;
    categoryId?: number | null;
    categoryName?: string | null;
}

export interface CategoryWriteRequest {
    name?: string;
    description?: string | null;
    industry?: string | null;
    sortOrder?: number | null;
}
