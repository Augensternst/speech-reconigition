# Development Environment Setup Guide

## Backend Requirements

- **Java Development Kit (JDK)**
  - Version 17 or higher
  - Required for running the Spring Boot application

- **Spring Boot**
  - Version 2.7.0 or later
  - Core framework for backend development

- **Maven**
  - Version 3.8 or higher
  - Used for dependency management and project build

- **MySQL**
  - Version 8.0 or higher
  - Primary database system

## Frontend Requirements

- **Node.js**
  - Version 16.0 or higher
  - Required for running the React application

- **React**
  - Version 18.0 or higher
  - Frontend framework

- **Package Manager**
  - npm or yarn
  - For managing frontend dependencies
  - WebSocket client support required

## AI Service Requirements

- **ZhipuAI Integration**
  - Valid API access credentials
  - Minimum 4GB RAM for AI model operations
  - Stable internet connection for API communications

## System Requirements

- **Hardware**
  - RAM: 8GB minimum (16GB recommended)
  - CPU: 4 cores or better
  - Storage: 20GB available space

- **Network**
  - Broadband internet connection

## Getting Started

### Frontend Setup
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

### Backend Setup
```bash
# Run the backend application
./mvnw spring-boot:run
# Or use your IDE to run BackendApplication.java
```
