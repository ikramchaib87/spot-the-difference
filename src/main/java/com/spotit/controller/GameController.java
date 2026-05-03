package com.spotit.controller;

import com.spotit.model.Player;
import com.spotit.model.SavedGame;
import com.spotit.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ResponseBody;

// Gère toutes les pages liées au jeu : menu, play, save, finish
@Controller
public class GameController {

    @Autowired
    private GameService gameService;

    // Affiche le menu principal avec les 5 niveaux et les infos du joueur
    @GetMapping("/menu")
    public String menu(HttpSession session, Model model) {

        Player player = getPlayerFromSession(session);
        if (player == null) return "redirect:/login";

        // Envoie les données du joueur à menu.html via le Model
        model.addAttribute("username", player.getUsername());
        model.addAttribute("bestScore", player.getBestScore());
        model.addAttribute("unlockedLevel", player.getUnlockedLevel());

        // Vérifie si le joueur a une partie sauvegardée pour afficher le bouton "Reprendre"
        SavedGame saved = gameService.getSavedGame(player);
        model.addAttribute("hasSavedGame", saved != null);
        if (saved != null) {
            model.addAttribute("savedLevel", saved.getLevel());
        }

        return "menu";
    }

    // Lance le niveau choisi par le joueur
    // @RequestParam récupère le numéro du niveau depuis l'URL : /play?level=1
    @GetMapping("/play")
    public String play(@RequestParam int level,
                       HttpSession session,
                       jakarta.servlet.http.HttpServletRequest request,
                       Model model) {

        Player player = getPlayerFromSession(session);
        if (player == null) return "redirect:/login";

        // Empêche le joueur d'accéder à un niveau qu'il n'a pas encore débloqué
        if (level > player.getUnlockedLevel()) {
            return "redirect:/menu";
        }

        model.addAttribute("level", level);
        model.addAttribute("differencesCount", gameService.getDifferencesCount(level));
        model.addAttribute("timeLimit", gameService.getTimeLimit(level));
        model.addAttribute("username", player.getUsername());

        // Le contextPath est nécessaire pour que fetch() en JavaScript
        // construise la bonne URL (ex: /spotit-1.0.0/save)
        model.addAttribute("contextPath", request.getContextPath());

        return "play";
    }

    // Reprend une partie sauvegardée en restaurant l'état du jeu
    @GetMapping("/resume")
    public String resume(HttpSession session,
                         jakarta.servlet.http.HttpServletRequest request,
                         Model model) {

        Player player = getPlayerFromSession(session);
        if (player == null) return "redirect:/login";

        SavedGame saved = gameService.getSavedGame(player);
        if (saved == null) return "redirect:/menu";

        // Restaure l'état exact de la partie : niveau, score, différences trouvées, temps restant
        model.addAttribute("level", saved.getLevel());
        model.addAttribute("differencesFound", saved.getDifferencesFound());
        model.addAttribute("currentScore", saved.getCurrentScore());
        model.addAttribute("timeRemaining", saved.getTimeRemaining());
        model.addAttribute("differencesCount", gameService.getDifferencesCount(saved.getLevel()));
        model.addAttribute("username", player.getUsername());
        model.addAttribute("contextPath", request.getContextPath());

        return "play";
    }

    // Sauvegarde la partie en cours — appelé automatiquement par fetch() en JavaScript
    // @ResponseBody retourne "ok" sans chercher une page HTML
    // Le joueur reste sur la page de jeu sans interruption
    @PostMapping("/save")
    @ResponseBody
    public String save(@RequestParam(defaultValue = "1") int level,
                       @RequestParam(defaultValue = "0") int differencesFound,
                       @RequestParam(defaultValue = "0") int currentScore,
                       @RequestParam(defaultValue = "0") int timeRemaining,
                       HttpSession session) {

    	Long playerId = (Long) session.getAttribute("playerId");
    	if (playerId == null) return "error";
    	gameService.saveGame(playerId, level, differencesFound, currentScore, timeRemaining);
        return "ok";
    }

    // Appelé quand le joueur termine une partie (toutes les différences trouvées ou bouton Terminer)
    @PostMapping("/finish")
    public String finish(@RequestParam(defaultValue = "0") int level,
                         @RequestParam(defaultValue = "0") int differencesFound,
                         @RequestParam(defaultValue = "0") int secondsUsed,
                         HttpSession session,
                         Model model) {

        // On récupère l'ID directement depuis la session pour éviter les objets détachés JPA
        Long playerId = (Long) session.getAttribute("playerId");
        if (playerId == null) return "redirect:/login";

        int score = gameService.calculateScore(level, differencesFound, secondsUsed);

        // On passe l'ID au service qui recharge le joueur depuis la BDD
        // Cela évite le problème d'objet détaché Hibernate qui empêchait les UPDATE
        gameService.unlockNextLevel(playerId, level, score);
        gameService.deleteSavedGameById(playerId);

        // On recharge le joueur depuis la BDD pour avoir les scores mis à jour
        Player player = gameService.getPlayerById(playerId);

        model.addAttribute("level", level);
        model.addAttribute("score", score);
        model.addAttribute("differencesFound", differencesFound);
        model.addAttribute("totalDifferences", gameService.getDifferencesCount(level));
        model.addAttribute("username", player.getUsername());
        model.addAttribute("secondsUsed", secondsUsed);

        // Au niveau 5, on envoie le récapitulatif complet de tous les niveaux
        if (level == 5) {
            model.addAttribute("scoreLevel1", player.getScoreLevel1());
            model.addAttribute("scoreLevel2", player.getScoreLevel2());
            model.addAttribute("scoreLevel3", player.getScoreLevel3());
            model.addAttribute("scoreLevel4", player.getScoreLevel4());
            model.addAttribute("scoreLevel5", score);
            int grandTotal = player.getScoreLevel1() + player.getScoreLevel2()
                           + player.getScoreLevel3() + player.getScoreLevel4() + score;
            model.addAttribute("grandTotal", grandTotal);
        }

        return "result";
    }

    // Méthode utilitaire : récupère le joueur connecté depuis la session
    // Retourne null si le joueur n'est pas connecté ou si la session a expiré
    private Player getPlayerFromSession(HttpSession session) {
        Long playerId = (Long) session.getAttribute("playerId");
        if (playerId == null) return null;
        return gameService.getPlayerById(playerId);
    }
}