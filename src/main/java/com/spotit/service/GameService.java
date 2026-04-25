package com.spotit.service;

import com.spotit.model.Player;
import com.spotit.model.SavedGame;
import com.spotit.repository.PlayerRepository;
import com.spotit.repository.SavedGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional 
public class GameService {

    // ✅ @Autowired = Spring injecte automatiquement le repository
    //    Tu n'as pas besoin de faire "new PlayerRepository()" toi-même
    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private SavedGameRepository savedGameRepository;

    // ─── INSCRIPTION ──────────────────────────────────────

    // ✅ Enregistre un nouveau joueur
    //    Retourne true si succès, false si username déjà pris
    public boolean register(String username, String password) {

        // Vérifie si le username existe déjà
        if (playerRepository.existsByUsername(username)) {
            return false; // username déjà pris
        }

        // Crée un nouveau joueur
        Player player = new Player();
        player.setUsername(username);
        player.setPassword(password);

        // Sauvegarde dans la BDD
        playerRepository.save(player);
        return true;
    }

    // ─── CONNEXION ────────────────────────────────────────

    // ✅ Vérifie username + password
    //    Retourne le joueur si correct, null si incorrect
    public Player login(String username, String password) {

        Player player = playerRepository.findByUsername(username);

        // Si joueur introuvable ou mauvais mot de passe → null
        if (player == null || !player.getPassword().equals(password)) {
            return null;
        }

        return player; // connexion réussie
    }

    // ─── NIVEAUX ──────────────────────────────────────────

    // ✅ Retourne le nombre de différences selon le niveau
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
    // ✅ Retourne le temps limite en secondes (0 = pas de chrono)
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

    // ✅ Calcule le score final selon le niveau et le temps mis
public int calculateScore(int level, int differencesFound, int secondsUsed) {

    if (differencesFound == 0) return 0;

    int totalDiff = getDifferencesCount(level);

    // Base : 500 points par différence trouvée
    int base = differencesFound * 500;

    // Bonus completion : +1000 si toutes trouvées
    int completionBonus = (differencesFound == totalDiff) ? 1000 : 0;

    // Bonus niveau : plus le niveau est élevé, plus le multiplicateur est grand
    double levelMultiplier = 1.0 + (level - 1) * 0.2; // 1.0 / 1.2 / 1.4 / 1.6 / 1.8

    // Pénalité temps : -3 points par seconde
    int timePenalty = secondsUsed * 3;

    int score = (int)((base + completionBonus) * levelMultiplier) - timePenalty;

    return Math.max(score, 0);
}


    // ─── DÉBLOQUER NIVEAU SUIVANT ─────────────────────────

    // ✅ Après avoir terminé un niveau, débloque le suivant
//✅ Prend un ID pas un Player — évite les objets détachés
public void unlockNextLevel(Long playerId, int completedLevel, int score) {

    Player player = playerRepository.findById(playerId).orElse(null);
    if (player == null) return;

    // ✅ Met à jour les valeurs en mémoire
    switch (completedLevel) {
        case 1: player.setScoreLevel1(score); break;
        case 2: player.setScoreLevel2(score); break;
        case 3: player.setScoreLevel3(score); break;
        case 4: player.setScoreLevel4(score); break;
        case 5: player.setScoreLevel5(score); break;
    }

    int newUnlockedLevel = player.getUnlockedLevel();
    if (score > 0 && completedLevel >= player.getUnlockedLevel() && completedLevel < 5) {
        newUnlockedLevel = completedLevel + 1;
    }

    int totalScore = player.getScoreLevel1() + player.getScoreLevel2()
                   + player.getScoreLevel3() + player.getScoreLevel4()
                   + player.getScoreLevel5();

    int bestScore = Math.max(player.getBestScore(), totalScore);

    // ✅ Update direct SQL — bypass les problèmes Hibernate
    playerRepository.updatePlayerStats(
        playerId,
        player.getScoreLevel1(), player.getScoreLevel2(),
        player.getScoreLevel3(), player.getScoreLevel4(),
        player.getScoreLevel5(),
        bestScore, newUnlockedLevel
    );
}

//✅ Supprime la sauvegarde par ID joueur
public void deleteSavedGameById(Long playerId) {
 Player player = playerRepository.findById(playerId).orElse(null);
 if (player == null) return;
 SavedGame saved = savedGameRepository.findByPlayer(player);
 if (saved != null) {
     savedGameRepository.delete(saved);
 }
}

    // ─── SAUVEGARDE ───────────────────────────────────────

    // ✅ Sauvegarde la partie en cours
    public void saveGame(Player player, int level, int differencesFound,
                         int currentScore, int timeRemaining) {

        // Si une sauvegarde existe déjà → on la remplace
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

    // ─── REPRENDRE ────────────────────────────────────────

    // ✅ Retourne la partie sauvegardée du joueur (ou null si aucune)
    public SavedGame getSavedGame(Player player) {
        return savedGameRepository.findByPlayer(player);
    }

    // ✅ Supprime la sauvegarde après que le joueur termine ou abandonne
    public void deleteSavedGame(Player player) {
        SavedGame saved = savedGameRepository.findByPlayer(player);
        if (saved != null) {
            savedGameRepository.delete(saved);
        }
    }

    // ─── JOUEUR ───────────────────────────────────────────

    // ✅ Récupère un joueur par son id (utile pour la session)
    public Player getPlayerById(Long id) {
        return playerRepository.findById(id).orElse(null);
    }
}