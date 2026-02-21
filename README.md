# Lumina Medical Center - Core Backend API

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Security](https://img.shields.io/badge/Security-JWT_Auth-DC382D?style=for-the-badge)

The robust, highly scalable backend service for the Lumina Hospital Management System. Built with **Spring Boot 3** and **Java 17**, this API follows a strictly modular architecture (Package-by-Feature) to ensure separation of concerns, testability, and readiness for future microservices extraction.

**Frontend Client Repository:** [Lumina Mobile Client](https://github.com/zeynepiseri/lumina_medical_center)

---

## Architecture & System Design

The codebase strictly adheres to **Domain-Driven Design (DDD)** concepts and a modular monolith approach. 

* **`modules/`**: Contains isolated business domains (`admin`, `appointment`, `auth`, `clinical`, `finance`, `medicalrecord`, `patient`, `profile`). Each module encapsulates its own Controllers, Services, Repositories, and DTOs.
* **`infrastructure/`**: Handles external integrations, security configurations (Spring Security), and global settings (OpenAPI, JWT filters, File Management).
* **`shared/`**: Contains cross-cutting concerns like global exception handling (`@ControllerAdvice`), unified API responses, audit logging, and email services.

### Database Inheritance Model
The system utilizes advanced JPA entity inheritance. A central `_user` table maps to specific roles (`patient`, `doctor`, `nurse`, `lab_technician`) using One-to-One relationships, ensuring normalized and secure data persistence.

<img width="1847" height="1433" alt="luminaBackend" src="https://github.com/user-attachments/assets/76bbb1bc-96bd-4664-a7bf-5547e7735e39" />

---

## Key Technical Features

* **Multi-Role RBAC:** Granular authorization handling 6 distinct roles (Admin, Doctor, Patient, Nurse, Lab Technician, Registrar) using stateless JWT authentication.
* **Clinical Workflows:** End-to-end management of doctor schedules, availability matrixes, and appointment lifecycles.
* **Medical Records & PDF:** Dynamic PDF generation for e-prescriptions (`PrescriptionPdfService`) and multipart file uploads for lab results.
* **Audit Logging:** Built-in interceptors to track entity modifications and critical API interactions for compliance.
* **Global Exception Handling:** Standardized error responses (`ApiErrorResponse`) eliminating stack-trace leaks to the client.

---

## Local Development & Setup

**Prerequisites:** * Java 17 (JDK)
* PostgreSQL database
* Docker (Optional for containerized run)

### 1. Environment Configuration
Create a `.env` file in the root directory (or export them in your terminal):
```env
DB_URL=jdbc:postgresql://<your-db-host>:<port>/<db_name>
DB_USERNAME=<your-username>
DB_PASSWORD=<your-password>
JWT_SECRET=<your-256-bit-secure-secret>
```

2. Running Locally (Maven)

``` bash
# Clean and compile dependencies
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```
3. Running with Docker

``` bash
# Clean and compile dependencies
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

API Documentation
The API is fully documented using OpenAPI 3.0. Once the server is running, navigate to:
👉 http://localhost:8080/swagger-ui.html
