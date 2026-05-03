package com.spotit.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Représente la table "saved_game" — stocke l'état d'une partie en cours
// Permet au joueur de quitter et reprendre sa partie plus tard
@Entity
@Table(name = "saved_game")
public class SavedGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lien vers le joueur propriétaire de cette sauvegarde
    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    // Le niveau en cours au moment de la sauvegarde (1 à 5)
    private int level;

    // Nombre de différences déjà trouvées avant la sauvegarde
    @Column(name = "differences_found")
    private int differencesFound = 0;

    // Score accumulé avant la sauvegarde
    @Column(name = "current_score")
    private int currentScore = 0;

    // Temps restant en secondes — utilisé pour les niveaux avec chronomètre
    @Column(name = "time_remaining")
    private int timeRemaining = 0;

    // Date et heure automatique de la sauvegarde
    @Column(name = "saved_at")
    private LocalDateTime savedAt = LocalDateTime.now();

    // Getters et Setters

    public Long getId() { return id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getDifferencesFound() { return differencesFound; }
    public void setDifferencesFound(int differencesFound) { this.differencesFound = differencesFound; }

    public int getCurrentScore() { return currentScore; }
    public void setCurrentScore(int currentScore) { this.currentScore = currentScore; }

    public int getTimeRemaining() { return timeRemaining; }
    public void setTimeRemaining(int timeRemaining) { this.timeRemaining = timeRemaining; }

    public LocalDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }
}