export type UserRole = 'USER' | 'ADMIN';

export interface RegisterRequest {
    email: string;
    password: string;
}

export interface RegisterResponse {
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

export interface GoogleAuthRequest {
    idToken: string;
}

export interface MessageResponse {
    message: string;
}
