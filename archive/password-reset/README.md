# Archived: forgot-password / reset-password email flow

Removed on 2026-07-29, same reason as [[email-verification]] archived earlier
the same day: no domain verified with Resend, so `sendPasswordResetEmail`
either fails silently (unverified custom domain) or is restricted to only the
site owner's own address (`onboarding@resend.dev` test domain) — useless for
real visitors who forget their password. Contact-form email is unaffected and
stays live, since it always sends *to* the owner's own address, which the
`onboarding@resend.dev` restriction actually permits.

Files here are exact pre-removal copies, mirroring their original repo-relative
paths under `backend/` and `frontend/`. To restore:

1. Copy each file here back to its original path (overwriting the simplified version).
2. Backend: re-add `forgotPassword()`/`resetPassword()` to `AuthService`, the
   two endpoints in `AuthController`, the two `permitAll()` entries in
   `SecurityConfig`, `passwordResetToken`/`passwordResetTokenExpiry` on `User`,
   `findByPasswordResetToken` in `UserRepository`, `sendPasswordResetEmail` in
   `EmailService`, and the `ForgotPasswordRequest`/`ResetPasswordRequest` DTOs
   + `reset-password-email.html` template.
3. Frontend: restore the `forgot-password` and `reset-password` feature
   folders, their routes in `app.routes.ts`, the "Forgot password?" link in
   `login.page.html`, and `forgotPassword()`/`resetPassword()` in
   `auth.service.ts` + their types.
4. Verify a domain in Resend and set `MAIL_FROM` to an address on it first —
   this flow is pointless without real delivery to arbitrary users.

## Files archived here

**Backend**
- `auth/application/AuthService.java` — full pre-removal version
- `auth/api/AuthController.java`
- `auth/persistence/UserRepository.java`
- `auth/domain/User.java`
- `auth/dto/ForgotPasswordRequest.java`
- `auth/dto/ResetPasswordRequest.java`
- `auth/application/EmailService.java`
- `shared/config/SecurityConfig.java`
- `email-templates/reset-password-email.html`
- Tests: `AuthServiceTest.java`, `AuthControllerTest.java`, `EmailServiceTest.java`, `UserRepositoryTest.java`

**Frontend**
- `features/auth/forgot-password/` (component + template)
- `features/auth/reset-password/` (component + template)
- `features/auth/login/ui/login.page.html` (had the "Forgot password?" link)
- `features/auth/register/services/auth.service.ts`
- `features/auth/register/types/index.ts`
- `app/app.routes.ts`
