package com.spotit.controller;

import com.spotit.model.Player;
import com.spotit.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

// ✅ @Controller dit à Spring : "cette classe gère des pages web"
@Controller
public class AuthController {

    // ✅ Spring injecte automatiquement le service
    @Autowired
    private GameService gameService;

    // ─── PAGE LOGIN ──────────────────────────────────────

    // ✅ GET /login → affiche la page login.html
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // cherche templates/login.html
    }

    // ✅ POST /login → traite le formulaire de connexion
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        // Appelle le service pour vérifier username/password
        Player player = gameService.login(username, password);

        if (player == null) {
            // ❌ Mauvais identifiants → on reste sur login avec message d'erreur
            model.addAttribute("error", "Username ou mot de passe incorrect !");
            return "login";
        }

        // ✅ Connexion réussie → on sauvegarde le joueur dans la session
        //    La session garde le joueur connecté pendant toute sa visite
        session.setAttribute("playerId", player.getId());
        session.setAttribute("username", player.getUsername());

        // Redirige vers le menu principal
        return "redirect:/menu";
    }

    // ─── PAGE REGISTER ───────────────────────────────────

    // ✅ GET /register → affiche la page register.html
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // ✅ POST /register → traite le formulaire d'inscription
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           Model model) {

        // Appelle le service pour créer le joueur
        boolean success = gameService.register(username, password);

        if (!success) {
            // ❌ Username déjà pris
            model.addAttribute("error", "Ce username est déjà pris !");
            return "register";
        }

        // ✅ Inscription réussie → redirige vers login
        return "redirect:/login";
    }

    // ─── DÉCONNEXION ─────────────────────────────────────

    // ✅ GET /logout → vide la session et redirige vers login
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // supprime toutes les données de session
        return "redirect:/login";
    }

    // ─── PAGE D'ACCUEIL ──────────────────────────────────

    // ✅ GET / → redirige vers login directement
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
}