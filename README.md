## Présentation du Projet
SpotIt est une application web où les utilisateurs comparent deux images pour identifier des différences. L'application inclut l'authentification, plusieurs niveaux, un système de score et la sauvegarde de la progression.
---

## Stack Technique
* **Backend :** Java 17 (JDK 17.0.17)
* **Framework :** Spring Framework 6 (Configuration Java manuelle - sans Spring Boot)
* **ORM :** Hibernate 6 (Implémentation JPA)
* **Frontend :** Thymeleaf, HTML, CSS, JavaScript
* **Base de données :** MySQL 8.0
* **Serveur :** Apache Tomcat 11
* **Outil de Build :** Maven

---

## Architecture (Strict MVC)
Ce projet utilise une configuration Java manuelle :
* **AppConfig.java :** Configure le contexte racine (DataSource, JPA/Hibernate, gestion des transactions et Services).
* **WebConfig.java :** Configure le contexte de la Servlet (Thymeleaf, gestion des ressources statiques et MVC).
* **WebAppInitializer.java :** Remplace le fichier `web.xml` pour démarrer la `DispatcherServlet` de Spring.

---

## Structure du Projet

### Backend (Java)
**com.spotit.config**
* `AppConfig` : Configuration de la base de données et des services.
* `WebConfig` : Configuration de Thymeleaf et du MVC.
* `WebAppInitializer` : Point d'entrée de l'application pour Tomcat.

**com.spotit.controller**
* `AuthController` : Logique d'inscription et de connexion.
* `GameController` : Navigation et flux du jeu.

**com.spotit.service**
* `GameService` : Logique métier, calcul du score et gestion des niveaux.

---

## 🛠 Installation et Déploiement

### 1. Base de données
Créer la base de données dans MySQL :
```sql
CREATE DATABASE spotit_db;
```

### 2. Configuration
Ouvrir `com.spotit.config.AppConfig.java` et mettre à jour les identifiants MySQL :
```java
ds.setUsername("root");
ds.setPassword("mot de passe");
```

### 3. Build (Génération du WAR)
Exécuter cette commande Maven à la racine du projet :
```bash
clean package -DskipTests
```

### 4. Déploiement sur Tomcat
1. Copier le fichier `spotit-1.0.0.war` généré dans le dossier `target/`.
2. Le coller dans le dossier `webapps/` de ton installation **Tomcat 11**.
3. Démarrer Tomcat via `startup.bat`.
4. Accéder au jeu : `http://localhost:8080/spotit-1.0.0/`

---

## ⚠️ NOTE Script de Correction SQL :
```sql
USE spotit_db;

-- Force MySQL à accepter la création de joueur sans score initial (met 0 par défaut)
ALTER TABLE player 
MODIFY COLUMN bestScore INT NOT NULL DEFAULT 0,
MODIFY COLUMN unlockedLevel INT NOT NULL DEFAULT 1,
MODIFY COLUMN scoreLevel1 INT NOT NULL DEFAULT 0,
MODIFY COLUMN scoreLevel2 INT NOT NULL DEFAULT 0,
MODIFY COLUMN scoreLevel3 INT NOT NULL DEFAULT 0,
MODIFY COLUMN scoreLevel4 INT NOT NULL DEFAULT 0,
MODIFY COLUMN scoreLevel5 INT NOT NULL DEFAULT 0;
```

---

## Logique de Score
Score = (Base + Bonus) × Multiplicateur - Pénalité de temps
* **Base :** 500 points par différence trouvée.
* **Pénalités :** -3 par seconde / -50 par clic incorrect.
* **Bonus :** +1000 pour la réussite du niveau.
