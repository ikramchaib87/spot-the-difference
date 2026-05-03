package com.spotit.model;

import jakarta.persistence.*;

// Cette classe représente la table "player" dans la base de données
// Chaque instance de Player = une ligne dans la table
@Entity
@Table(name = "player")
public class Player {

    // Clé primaire auto-incrémentée (1, 2, 3...)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Deux joueurs ne peuvent pas avoir le même username
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // Meilleur score total du joueur (somme des 5 niveaux)
    private int bestScore = 0;

    // Niveau actuellement débloqué — commence à 1
    // S'incrémente quand le joueur termine un niveau avec score > 0
    private int unlockedLevel = 1;

    // Score de chaque niveau sauvegardé séparément en BDD
    private int scoreLevel1 = 0;
    private int scoreLevel2 = 0;
    private int scoreLevel3 = 0;
    private int scoreLevel4 = 0;
    private int scoreLevel5 = 0;

    // Getters et Setters — permettent à Spring et Hibernate d'accéder aux champs privés

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getBestScore() { return bestScore; }
    public void setBestScore(int bestScore) { this.bestScore = bestScore; }

    public int getUnlockedLevel() { return unlockedLevel; }
    public void setUnlockedLevel(int unlockedLevel) { this.unlockedLevel = unlockedLevel; }

    public int getScoreLevel1() { return scoreLevel1; }
    public void setScoreLevel1(int s) { this.scoreLevel1 = s; }

    public int getScoreLevel2() { return scoreLevel2; }
    public void setScoreLevel2(int s) { this.scoreLevel2 = s; }

    public int getScoreLevel3() { return scoreLevel3; }
    public void setScoreLevel3(int s) { this.scoreLevel3 = s; }

    public int getScoreLevel4() { return scoreLevel4; }
    public void setScoreLevel4(int s) { this.scoreLevel4 = s; }

    public int getScoreLevel5() { return scoreLevel5; }
    public void setScoreLevel5(int s) { this.scoreLevel5 = s; }
}