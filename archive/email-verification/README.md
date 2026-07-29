# Archived: sign-up email verification

Removed on 2026-07-29 because production email couldn't be verified (no domain
owned yet to authenticate with Resend — see chat history for context). Login was
never gated on `emailVerified`, so removing this only drops the "confirm your
email" step after registration; new accounts are just marked verified immediately.

Files here are exact pre-removal copies, mirroring their original repo-relative
paths under `backend/` and `frontend/`. To restore:

1. Copy each file here back to its original path (overwriting the simplified version).
2. Backend: re-add `EmailService` verification-token generation to
   `AuthService.register()`, restore `resendVerification()`/`verifyEmail()`/
   `purgeExpiredUnverifiedUsers()` methods, restore the two endpoints in
   `AuthController`, restore the two `permitAll()` entries in `SecurityConfig`,
   restore `findByVerificationToken`/`deleteByEmailVerifiedFalseAndVerificationTokenExpiryBefore`
   in `UserRepository`, restore `verificationToken`/`verificationTokenExpiry`
   fields on `User`, restore `RegisterResponse.link` and `ResendVerificationRequest`.
3. Frontend: restore the `confirm-email` feature folder, its route in
   `app.routes.ts`, the "Check your inbox" branch in `register.page.ts`/`.html`,
   and `resendVerification()`/`verifyEmail()` in `auth.service.ts` + their types.
4. Re-verify a domain in Resend and set `MAIL_FROM` to an address on it before
   turning this back on — sending verification emails requires that either way.

## Files archived here

**Backend**
- `auth/application/AuthService.java` — full pre-removal version
- `auth/api/AuthController.java`
- `auth/persistence/UserRepository.java`
- `auth/domain/User.java`
- `auth/dto/RegisterResponse.java`
- `auth/dto/ResendVerificationRequest.java`
- `auth/application/EmailService.java`
- `shared/config/SecurityConfig.java`
- `email-templates/verification-email.html`
- Tests: `AuthServiceTest.java`, `AuthControllerTest.java`, `UserRepositoryTest.java`

**Frontend**
- `features/auth/confirm-email/` (component + template)
- `features/auth/register/components/register.page.ts`
- `features/auth/register/ui/register.page.html`
- `features/auth/register/services/auth.service.ts`
- `features/auth/register/types/index.ts`
- `app/app.routes.ts`
