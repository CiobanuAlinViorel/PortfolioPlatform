# Portfolio Platform

A full-stack portfolio management platform with role-based access control, 
built with Java Spring Boot (backend) and Angular (frontend).

## Live Demo

- **Frontend:** [alinv-portfolio.netlify.app](https://alinv-portfolio.netlify.app)
- **Backend:** Hosted on Render (free tier — first load may take ~15s to wake up)

## Tech Stack

**Backend**
- Java 21, Spring Boot
- Spring Security + JWT authentication
- JPA / Hibernate
- PostgreSQL
- Flyway database migrations
- Maven

**Frontend**
- Angular
- TypeScript
- Tailwind CSS

**Infrastructure**
- Backend: Render
- Frontend: Netlify

## Features

- Role-based access (admin / visitor)
- Admin dashboard to manage projects, skills, and experience
- REST API with JWT-secured endpoints
- Database migrations with Flyway
- DTO pattern, layered architecture (Controller → Service → Repository)
- Exception handling and input validation

## Project Structure

PortfolioPlatform/
├── backend/ # Spring Boot REST API
├── frontend/ # Angular application
├── docs/ # Plans and diagrams
└── render.yaml # Render deployment config

## Docs

- `docs/MODERN_UI_PLAN.md` — visual/interaction modernization plan for the public site
- `docs/UI_UX_IMPROVEMENT_PLAN.md` — structural UI/UX rules (a11y, data, component APIs)
- `docs/db-schema.png` — database diagram


## Running Locally

### Backend
```bash
cd backend
./mvnw spring-boot:run
```
Requires PostgreSQL running locally. Configure credentials in `application.properties`.

### Frontend
```bash
cd frontend
npm install
ng serve
```

## Author

Alin-Viorel Ciobanu — [LinkedIn](https://www.linkedin.com/in/alin-viorel-ciobanu-84b06b269/) 
| [GitHub](https://github.com/CiobanuAlinViorel)