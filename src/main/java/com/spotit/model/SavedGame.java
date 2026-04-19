package com.spotit.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// ✅ Cette table garde l'état d'une partie en cours
@Entity
@Table(name = "saved_game")
public class SavedGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Lien vers le joueur : plusieurs sauvegardes peuvent appartenir au même joueur
    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    // ✅ Le niveau en cours (1 à 5)
    private int level;

    // ✅ Nombre de différences déjà trouvées
    private int differencesFound = 0;

    // ✅ Score actuel au moment de la sauvegarde
    private int currentScore = 0;

    // ✅ Temps restant en secondes (pour les niveaux avec chrono)
    private int timeRemaining = 0;

    // ✅ Date et heure de la sauvegarde
    private LocalDateTime savedAt = LocalDateTime.now();

    // ─── Getters & Setters ───────────────────────────────

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