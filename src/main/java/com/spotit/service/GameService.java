package com.spotit.service;

import com.spotit.model.Player;
import com.spotit.model.SavedGame;
import com.spotit.repository.PlayerRepository;
import com.spotit.repository.SavedGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Contient toute la logique métier du jeu
// @Transactional garantit que chaque méthode s'exécute dans une transaction BDD
// Si une erreur arrive au milieu, toutes les opérations sont annulées
@Service
@Transactional
public class GameService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private SavedGameRepository savedGameRepository;

    // ─── INSCRIPTION ──────────────────────────────────────

    // Retourne true si l'inscription réussit, false si le username est déjà pris
    public boolean register(String username, String password) {

        if (playerRepository.existsByUsername(username)) {
            return false;
        }

        Player player = new Player();
        player.setUsername(username);
        player.setPassword(password);
        playerRepository.save(player);
        return true;
    }

    // ─── CONNEXION ────────────────────────────────────────

    // Vérifie que le username et le password correspondent à un joueur en BDD
    // Retourne le joueur si correct, null si introuvable ou mot de passe incorrect
    public Player login(String username, String password) {

        Player player = playerRepository.findByUsername(username);

        if (player == null || !player.getPassword().equals(password)) {
            return null;
        }

        return player;
    }

    // ─── NIVEAUX ──────────────────────────────────────────

    // Tous les niveaux ont 10 différences à trouver
    public int getDifferencesCount(int level) {
        switch (level) {
            case 1: return 10;
            case 2: return 10;
            case 3: return 10;
            case 4: return 10;
            case 5: return 10;
            default: return 10;
        }
    }

    // Retourne le temps limite en secondes selon le niveau
    // 0 = pas de chronomètre pour ce niveau
    public int getTimeLimit(int level) {
        switch (level) {
            case 1: return 0;   // pas de chrono
            case 2: return 0;   // pas de chrono
            case 3: return 150; // 2 minutes 30
            case 4: return 120; // 2 minutes
            case 5: return 90;  // 1 minute 30
            default: return 0;
        }
    }

    // ─── SCORE ────────────────────────────────────────────

    // Calcule le score final selon la formule :
    // Score = (Base + Bonus completion) × Multiplicateur niveau - Pénalité temps
    public int calculateScore(int level, int differencesFound, int secondsUsed) {

        if (differencesFound == 0) return 0;

        int totalDiff = getDifferencesCount(level);

        // 500 points par différence trouvée
        int base = differencesFound * 500;

        // +1000 si le joueur a trouvé toutes les différences
        int completionBonus = (differencesFound == totalDiff) ? 1000 : 0;

        // Multiplicateur croissant selon le niveau : x1.0 à x1.8
        // Récompense les niveaux plus difficiles
        double levelMultiplier = 1.0 + (level - 1) * 0.2;

        // -3 points par seconde utilisée
        int timePenalty = secondsUsed * 3;

        int score = (int)((base + completionBonus) * levelMultiplier) - timePenalty;

        // Le score ne peut pas être négatif
        return Math.max(score, 0);
    }

    // ─── DÉBLOQUER NIVEAU SUIVANT ─────────────────────────

    // Prend l'ID du joueur (pas l'objet Player) pour éviter les problèmes
    // d'objets détachés Hibernate — l'objet venant de la session HTTP
    // n'est plus géré par JPA, donc save() ne fonctionnerait pas
    public void unlockNextLevel(Long playerId, int completedLevel, int score) {

        // Recharge le joueur directement depuis la BDD
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null) return;

        // Sauvegarde le score du niveau terminé
        switch (completedLevel) {
            case 1: player.setScoreLevel1(score); break;
            case 2: player.setScoreLevel2(score); break;
            case 3: player.setScoreLevel3(score); break;
            case 4: player.setScoreLevel4(score); break;
            case 5: player.setScoreLevel5(score); break;
        }

        // Débloque le niveau suivant seulement si le score est > 0
        int newUnlockedLevel = player.getUnlockedLevel();
        if (score > 0 && completedLevel >= player.getUnlockedLevel() && completedLevel < 5) {
            newUnlockedLevel = completedLevel + 1;
        }

        // Calcule le score total sur tous les niveaux
        int totalScore = player.getScoreLevel1() + player.getScoreLevel2()
                       + player.getScoreLevel3() + player.getScoreLevel4()
                       + player.getScoreLevel5();

        int bestScore = Math.max(player.getBestScore(), totalScore);

        // Utilise @Modifying (requête directe) au lieu de save()
        // pour éviter le problème d'objet détaché Hibernate
        playerRepository.updatePlayerStats(
            playerId,
            player.getScoreLevel1(), player.getScoreLevel2(),
            player.getScoreLevel3(), player.getScoreLevel4(),
            player.getScoreLevel5(),
            bestScore, newUnlockedLevel
        );
    }

    // Supprime la sauvegarde d'un joueur en utilisant son ID
    public void deleteSavedGameById(Long playerId) {
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null) return;
        SavedGame saved = savedGameRepository.findByPlayer(player);
        if (saved != null) {
            savedGameRepository.delete(saved);
        }
    }

    // ─── SAUVEGARDE ───────────────────────────────────────

    // Sauvegarde ou met à jour la partie en cours du joueur
    // Un joueur ne peut avoir qu'une seule sauvegarde à la fois
    public void saveGame(Player player, int level, int differencesFound,
                         int currentScore, int timeRemaining) {

        // Si une sauvegarde existe déjà, on la met à jour au lieu d'en créer une nouvelle
        SavedGame saved = savedGameRepository.findByPlayer(player);
        if (saved == null) {
            saved = new SavedGame();
            saved.setPlayer(player);
        }

        saved.setLevel(level);
        saved.setDifferencesFound(differencesFound);
        saved.setCurrentScore(currentScore);
        saved.setTimeRemaining(timeRemaining);

        savedGameRepository.save(saved);
    }

    // Retourne la partie sauvegardée du joueur, ou null s'il n'en a pas
    public SavedGame getSavedGame(Player player) {
        return savedGameRepository.findByPlayer(player);
    }

    // Supprime la sauvegarde quand le joueur termine ou abandonne une partie
    public void deleteSavedGame(Player player) {
        SavedGame saved = savedGameRepository.findByPlayer(player);
        if (saved != null) {
            savedGameRepository.delete(saved);
        }
    }

    public void saveGame(Long playerId, int level, int differencesFound,
            int currentScore, int timeRemaining) {

Player player = playerRepository.findById(playerId).orElse(null);
if (player == null) return;

SavedGame saved = savedGameRepository.findByPlayer(player);
if (saved == null) {
saved = new SavedGame();
saved.setPlayer(player);
}

saved.setLevel(level);
saved.setDifferencesFound(differencesFound);
saved.setCurrentScore(currentScore);
saved.setTimeRemaining(timeRemaining);

savedGameRepository.save(saved);
}
    // ─── JOUEUR ───────────────────────────────────────────

    // Récupère un joueur depuis la BDD par son ID
    // Utilisé après chaque requête pour recharger les données à jour
    public Player getPlayerById(Long id) {
        return playerRepository.findById(id).orElse(null);
    }
}