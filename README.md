
# SpotIt – Spot the Difference Game

## Project Overview

SpotIt is a web application where users compare two images and identify differences.
The application includes authentication, multiple levels, scoring, and game state saving.

---

## Main Features

* User registration and login
* 5 levels with increasing difficulty
* Score system with bonus and penalties
* Timed levels (level 3 and 5)
* Automatic save and resume functionality
* Progressive level unlocking

---

## Tech Stack

* Backend: Spring Boot (Java 17)
* Frontend: Thymeleaf, HTML, CSS, JavaScript
* Database: MySQL
* Build Tool: Maven

---

## Architecture

The project follows the MVC pattern:

* Model: JPA entities representing database tables
* View: Thymeleaf templates (HTML pages)
* Controller: Handles HTTP requests and navigation
* Service: Contains business logic
* Repository: Handles database operations

---

## Project Structure

### Backend (Java)

**com.spotit**

* controller

  * AuthController: authentication (login, register, logout)
  * GameController: game flow (menu, play, save, finish, resume)

* model

  * Player: stores user data and scores
  * SavedGame: stores game progress

* repository

  * PlayerRepository: user queries
  * SavedGameRepository: saved game queries

* service

  * GameService: core logic (score calculation, level management, save/load)

* SpotItApplication

  * Application entry point

---

### Frontend

**templates (Thymeleaf)**

* login.html
* register.html
* menu.html
* play.html
* result.html

**static**

* css/style.css
* js/game.js
* images/ (game levels)

---

## Game Logic

* Player clicks on the modified image
* JavaScript calculates click position
* Position is compared to predefined difference zones
* Correct click → validated
* Incorrect click → penalty
* Score is updated dynamically

---

## Backend Flow

* User selects a level → `/play?level=X`
* Controller checks if level is unlocked
* Game starts (frontend handles interaction)
* Game ends → `/finish`
* Score is calculated in GameService
* Level progression is updated

---

## Database

### Tables

---

**player**

* id
* username
* password
* unlockedLevel
* bestScore
* scoreLevel1 → scoreLevel5

**saved_game**

* id
* player_id
* level
* differences_found
* current_score
* time_remaining
* saved_at

---

## Scoring Logic

Score = (Base + Bonus) × Multiplier - Time Penalty

* Base = 500 × differences found
* Bonus = +1000 if level completed
* Multiplier = depends on level (1.0 → 1.8)
* Time penalty = -3 per second
* Wrong click = -50

---

## Key Functional Points

* Levels are unlocked progressively
* Game state is auto-saved every 30 seconds
* Saved game can be resumed from menu
* Frontend handles interaction (click detection)
* Backend handles validation, score, and persistence

---

## How to Run

1. Create database:

```
CREATE DATABASE spotit_db;
```

2. Configure `application.properties`:

```
spring.datasource.username=root
spring.datasource.password=your_sql_password
```

3. Run:

* Launch `SpotItApplication.java`

4. Open:

```
http://localhost:8080
```
