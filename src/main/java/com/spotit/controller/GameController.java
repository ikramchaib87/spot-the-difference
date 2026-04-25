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

    @GetMapping("/menu")
    public String menu(HttpSession session, Model model) {

        Player player = getPlayerFromSession(session);
        if (player == null) return "redirect:/login";

        model.addAttribute("username", player.getUsername());
        model.addAttribute("bestScore", player.getBestScore());
        model.addAttribute("unlockedLevel", player.getUnlockedLevel());

        SavedGame saved = gameService.getSavedGame(player);
        model.addAttribute("hasSavedGame", saved != null);
        if (saved != null) {
            model.addAttribute("savedLevel", saved.getLevel());
        }

        return "menu";
    }

    // ─── LANCER UNE PARTIE ───────────────────────────────

    @GetMapping("/play")
    public String play(@RequestParam int level,
                       HttpSession session,
                       jakarta.servlet.http.HttpServletRequest request,
                       Model model) {

        Player player = getPlayerFromSession(session);
        if (player == null) return "redirect:/login";

        if (level > player.getUnlockedLevel()) {
            return "redirect:/menu";
        }

        model.addAttribute("level", level);
        model.addAttribute("differencesCount", gameService.getDifferencesCount(level));
        model.addAttribute("timeLimit", gameService.getTimeLimit(level));
        model.addAttribute("username", player.getUsername());
        model.addAttribute("contextPath", request.getContextPath());

        return "play";
    }

    // ─── REPRENDRE UNE PARTIE ────────────────────────────

    @GetMapping("/resume")
    public String resume(HttpSession session,
                         jakarta.servlet.http.HttpServletRequest request,
                         Model model) {

        Player player = getPlayerFromSession(session);
        if (player == null) return "redirect:/login";

        SavedGame saved = gameService.getSavedGame(player);
        if (saved == null) return "redirect:/menu";

        model.addAttribute("level", saved.getLevel());
        model.addAttribute("differencesFound", saved.getDifferencesFound());
        model.addAttribute("currentScore", saved.getCurrentScore());
        model.addAttribute("timeRemaining", saved.getTimeRemaining());
        model.addAttribute("differencesCount", gameService.getDifferencesCount(saved.getLevel()));
        model.addAttribute("username", player.getUsername());
        model.addAttribute("contextPath", request.getContextPath());

        return "play";
    }

    // ─── SAUVEGARDER ─────────────────────────────────────

    @PostMapping("/save")
    @ResponseBody
    public String save(@RequestParam(defaultValue = "1") int level,
                       @RequestParam(defaultValue = "0") int differencesFound,
                       @RequestParam(defaultValue = "0") int currentScore,
                       @RequestParam(defaultValue = "0") int timeRemaining,
                       HttpSession session) {

        Player player = getPlayerFromSession(session);
        if (player == null) return "error";

        gameService.saveGame(player, level, differencesFound,
                             currentScore, timeRemaining);

        return "ok";
    }

    // ─── TERMINER UNE PARTIE ─────────────────────────────

    @PostMapping("/finish")
    public String finish(@RequestParam(defaultValue = "0") int level,
                         @RequestParam(defaultValue = "0") int differencesFound,
                         @RequestParam(defaultValue = "0") int secondsUsed,
                         HttpSession session,
                         Model model) {

        // ✅ Récupère l'ID depuis la session directement
        Long playerId = (Long) session.getAttribute("playerId");
        if (playerId == null) return "redirect:/login";

        // ✅ Calcule le score
        int score = gameService.calculateScore(level, differencesFound, secondsUsed);

        // ✅ Passe l'ID — le service recharge le joueur depuis BDD
        gameService.unlockNextLevel(playerId, level, score);
        gameService.deleteSavedGameById(playerId);

        // ✅ Recharge le joueur après sauvegarde
        Player player = gameService.getPlayerById(playerId);

        model.addAttribute("level", level);
        model.addAttribute("score", score);
        model.addAttribute("differencesFound", differencesFound);
        model.addAttribute("totalDifferences", gameService.getDifferencesCount(level));
        model.addAttribute("username", player.getUsername());
        model.addAttribute("secondsUsed", secondsUsed);

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

    private Player getPlayerFromSession(HttpSession session) {
        Long playerId = (Long) session.getAttribute("playerId");
        if (playerId == null) return null;
        return gameService.getPlayerById(playerId);
    }
}