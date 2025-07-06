# Rabobank Authorizations Assignment

This project implements a microservice-based system for managing read/write authorizations to Rabobank accounts. It uses
Java 11, Spring Boot, and MongoDB, and is structured in a modular fashion for clarity and extensibility.

---

## Table of Contents

- [Overview](#overview)
- [Project Structure](#project-structure)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [Run MongoDB](#run-mongodb)
  - [Build](#build)
- [API Endpoints](#api-endpoints)

---

## Overview

The Rabobank Authorizations Assignment provides functionality to:

- Register and manage Powers of Attorney (POA) between customers
- Check access rights to accounts

---

## Project Structure

```plaintext
RabobankAssignment
├── api
│   └── Exposes REST endpoints
├── data
│   └── MongoDB configuration and repositories
└── domain
    └── Core business models
```

api: Spring Boot application exposing account and authorization endpoints

data: Spring Data MongoDB repositories and configuration

domain: POJOs representing core business concepts

---

## Technologies

- Java 11

- Spring Boot

- Spring Data MongoDB

- Maven

- Docker (optional, for local MongoDB)

- Git

- Prerequisites
  Java 11

- Maven 3.6+

---

## Getting Started

### Run MongoDB

You can run MongoDB in Docker:

```bash
docker run -d -p 27017:27017 mongo
```

or install MongoDB locally and ensure it is listening on localhost:27017.

### Build

From the project root:

```bash
mvn clean install
```

---

## API Endpoints

Below are the supported endpoints in this project:

### 1.Authorization

- **POST** `/auth/login`

  *Request Body example:*
  ```json
  {
     "username": "username",
     "password": "password"
  }
  ```
  **Response Example:**
  ```json
  {
     "token": "jwt_token_here"
  }
  ```
### 2.Power of Attorney(POA)

- **POST** `/api/poa`

  *Request Body example:*
  ```json
  {
     "grantor": "Alice",
     "grantee": "Bob",
     "accountNumber": "123456789",
     "accountHolderName": "Alice",
     "accountType": "SAVING",         // or "PAYMENT"
     "initialBalance": 1000.0,
     "authorizationType": "READ"      // or "WRITE"
  }
  ```
  **Response Example:**
  ```json
  {
     "id": "60d21b4667d0d8992e610c85",
     "grantor": "Alice",
     "grantee": "Bob",
     "accountNumber": "123456789",
     "accountType": "SAVING",
     "authorizationType": "READ"
  }
  ```
### 2.Power of Attorney(POA)

- **GET** `/api/poa?grantee={granteeName}`

  *Query Parameter: the name of the user receiving access*

  **Response Example:**
  ```json
  [
    {
      "grantor": "Alice",
      "accountNumber": "123456789",
      "accountHolderName": "Alice Johnson",
      "accountType": "SAVING",
      "authorizationType": "READ"
    },
    {
      "grantor": "Charlie",
      "accountNumber": "123456789",
      "accountHolderName": "Charlie",
      "accountType": "PAYMENT",
      "authorizationType": "WRITE"
    }
  ]
  ```



