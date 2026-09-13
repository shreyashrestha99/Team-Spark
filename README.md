# Team-Spark

Islington Hackathon 2026 — a website that makes class timetables and exam seating
plans for Islington College automatically, instead of doing it by hand.

---

## Tech Stack

**Backend**
- Java 17
- Spring Boot 4.1
- Spring Security (login with JWT tokens, passwords hashed with BCrypt)
- Spring Data JPA / Hibernate
- Maven

**Database**
- PostgreSQL 18

**Frontend**
- React 19
- TypeScript
- Vite
- Tailwind CSS 4
- React Router
- Axios
- Lucide (icons)

**Charts**
- Made by hand using SVG (no chart library)

**Timetable maker**
- Our own code that tries different time slots until it finds a week with no
  clashes, then keeps improving it to remove gaps between classes

**Exam seating**
- Our own code that picks the exam halls and gives every student a seat, keeping
  students who sit the same paper away from each other

---

## AI Tools We Used

| Tool | What we used it for |
|---|---|
| **ChatGPT** | Research — reading about how timetable problems are usually solved |
| **Claude (Claude Code)** | Coding — writing the backend, the React pages, and fixing bugs |
| **Google Search** | Looking up documentation and writing the report |

We checked and tested all the code the AI wrote. The rules for the timetable, the
seating idea and the database design were decided by our team.

---

## How to Run It

You need **Java 17+**, **Node.js** and **PostgreSQL** installed.

**1. Make the database**

Open PostgreSQL and create a database called `Team Spark`.

**2. Start the backend**

```bash
cd Backend
./mvnw spring-boot:run
```

It runs on http://localhost:8081

**3. Start the frontend**

Open a second terminal:

```bash
cd Frontend
npm install
npm run dev
```

It runs on http://localhost:5173

**4. Log in**

Open http://localhost:5173 and log in with:

- Username: `admin`
- Password: `admin123`

The first time it starts, it fills the database with sample data — students,
teachers, rooms, modules and batches — so you can try it straight away.

---

## Note

The database password and the login secret key are still written inside
`Backend/src/main/resources/application.yaml`. Change them before putting this
online.
