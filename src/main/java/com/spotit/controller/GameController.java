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
@Controller
public class GameController {

    @Autowired
    private GameService gameService;

    // ─── MENU PRINCIPAL ──────────────────────────────────

    // ✅ GET /menu → affiche le menu avec les 5 niveaux
    @GetMapping("/menu")
    public String menu(HttpSession session, Model model) {

        // Vérifie que le joueur est connecté
        Player player = getPlayerFromSession(session);
        if (player == null) return "redirect:/login";

        // Envoie les infos du joueur à la page HTML
        model.addAttribute("username", player.getUsername());
        model.addAttribute("bestScore", player.getBestScore());
        model.addAttribute("unlockedLevel", player.getUnlockedLevel());

        // Vérifie s'il a une partie sauvegardée
        SavedGame saved = gameService.getSavedGame(player);
        model.addAttribute("hasSavedGame", saved != null);
        if (saved != null) {
            model.addAttribute("savedLevel", saved.getLevel());
        }

        return "menu"; // templates/menu.html
    }

    // ─── LANCER UNE PARTIE ───────────────────────────────

    // ✅ GET /play?level=1 → lance le niveau choisi
    @GetMapping("/play")
    public String play(@RequestParam int level,
                       HttpSession session,
                       Model model) {

        Player player = getPlayerFromSession(session);
        if (player == null) return "redirect:/login";

        // Vérifie que le joueur a débloqué ce niveau
        if (level > player.getUnlockedLevel()) {
            return "redirect:/menu";
        }

        // Envoie les infos du niveau à la page HTML
        model.addAttribute("level", level);
        model.addAttribute("differencesCount", gameService.getDifferencesCount(level));
        model.addAttribute("timeLimit", gameService.getTimeLimit(level));
        model.addAttribute("username", player.getUsername());

        return "play"; // templates/play.html
    }

    // ─── REPRENDRE UNE PARTIE ────────────────────────────

    // ✅ GET /resume → reprend la partie sauvegardée
    @GetMapping("/resume")
    public String resume(HttpSession session, Model model) {

        Player player = getPlayerFromSession(session);
        if (player == null) return "redirect:/login";

        SavedGame saved = gameService.getSavedGame(player);
        if (saved == null) return "redirect:/menu";

        // Envoie l'état sauvegardé à la page
        model.addAttribute("level", saved.getLevel());
        model.addAttribute("differencesFound", saved.getDifferencesFound());
        model.addAttribute("currentScore", saved.getCurrentScore());
        model.addAttribute("timeRemaining", saved.getTimeRemaining());
        model.addAttribute("differencesCount",
                           gameService.getDifferencesCount(saved.getLevel()));
        model.addAttribute("username", player.getUsername());

        return "play";
    }

    // ─── SAUVEGARDER ─────────────────────────────────────

    // ✅ POST /save → sauvegarde la partie en cours
    @PostMapping("/save")
    @ResponseBody  // ✅ Retourne du texte simple, pas une page HTML
    public String save(@RequestParam(defaultValue = "1") int level,
                       @RequestParam(defaultValue = "0") int differencesFound,
                       @RequestParam(defaultValue = "0") int currentScore,
                       @RequestParam(defaultValue = "0") int timeRemaining,
                       HttpSession session) {

        Player player = getPlayerFromSession(session);
        if (player == null) return "error";

        gameService.saveGame(player, level, differencesFound,
                             currentScore, timeRemaining);

        return "ok"; // ✅ Juste "ok", pas de redirection
    }

    // ─── TERMINER UNE PARTIE ─────────────────────────────

    // ✅ POST /finish → calcule le score et affiche le résultat
    @PostMapping("/finish")
public String finish(@RequestParam(defaultValue = "0") int level,
                     @RequestParam(defaultValue = "0") int differencesFound,
                     @RequestParam(defaultValue = "0") int secondsUsed,
                     HttpSession session,
                     Model model) {

    Player player = getPlayerFromSession(session);
    if (player == null) return "redirect:/login";

    int score = gameService.calculateScore(level, differencesFound, secondsUsed);
    gameService.unlockNextLevel(player, level, score);
    gameService.deleteSavedGame(player);

    // Recharge le joueur depuis la BDD pour avoir les scores à jour
    player = gameService.getPlayerById(player.getId());

    model.addAttribute("level", level);
    model.addAttribute("score", score);
    model.addAttribute("differencesFound", differencesFound);
    model.addAttribute("totalDifferences", gameService.getDifferencesCount(level));
    model.addAttribute("username", player.getUsername());
    model.addAttribute("secondsUsed", secondsUsed);

    // ✅ Si niveau 5 → envoie tous les scores
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

    // ─── MÉTHODE UTILITAIRE ──────────────────────────────

    // ✅ Récupère le joueur depuis la session
    //    Retourne null si le joueur n'est pas connecté
    private Player getPlayerFromSession(HttpSession session) {
        Long playerId = (Long) session.getAttribute("playerId");
        if (playerId == null) return null;
        return gameService.getPlayerById(playerId);
    }
}