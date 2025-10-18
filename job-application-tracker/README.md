# Job Application Tracker

## Overview
The Job Application Tracker is a backend project built with **Java Spring Boot** and **MySQL**.  
It allows users to track job applications, including company name, position, and application status, through a REST API.

## Features
- Add new job applications (POST)
- View all job applications (GET)
- Update application status (PUT)
- Delete job applications (DELETE)
- Field validation for required attributes
- Connected to a MySQL database for persistent storage

## Tech Stack
- **Backend:** Java Spring Boot, Hibernate, Maven  
- **Database:** MySQL  
- **Other Tools:** Postman for API testing

## Getting Started

### Prerequisites
- Java 25 (Temurin)  
- Maven  
- MySQL Server

### Backend Setup
1. Clone this repository.
2. Navigate to the `backend` folder.
3. Configure `application.properties` with your MySQL credentials.
4. Run the backend server:

### Frontend Setup
1. npm install
2. npm start
The frontend will connect to the backend API and allow you to manage job applications through a browser.

### REST API Endpoints
# Method       # Endpoint       # Description
GET            /api/jobs        Retrieve all job applications
POST           /api/jobs        Add a new job application
PUT            /api/jobs/{id}   Update a job application by ID
DELETE         /api/jobs/{id}   Delete a job application by ID

```bash
./mvnw spring-boot:run   # Linux/Mac
mvnw spring-boot:run     # Windows
