export type UserRole = 'USER' | 'ADMIN';

export interface RegisterRequest {
    email: string;
    password: string;
}

export interface RegisterResponse {
    link: string;
    email: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface AuthResponse {
    token: string;
    id: number;
    email: string;
    role: UserRole;
    emailVerified: boolean;
}

export interface ForgotPasswordRequest {
    email: string;
}

export interface ResetPasswordRequest {
    token: string;
    newPassword: string;
}

export interface ResendVerificationRequest {
    email: string;
}

export interface MessageResponse {
    message: string;
}
