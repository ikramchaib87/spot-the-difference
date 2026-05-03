package com.spotit.controller;

import com.spotit.model.Player;
import com.spotit.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

// Gère toutes les pages liées à l'authentification : login, register, logout
@Controller
public class AuthController {

    // Spring injecte automatiquement le service sans qu'on fasse "new GameService()"
    @Autowired
    private GameService gameService;

    // Affiche la page de connexion
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // Traite le formulaire de connexion
    // @RequestParam récupère les valeurs des champs <input> du formulaire HTML
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        Player player = gameService.login(username, password);

        if (player == null) {
            // Mauvais identifiants : on renvoie vers login avec un message d'erreur
            model.addAttribute("error", "Username ou mot de passe incorrect !");
            return "login";
        }

        // Connexion réussie : on stocke l'ID du joueur dans la session HTTP
        // La session permet de savoir qui est connecté sur toutes les pages suivantes
        session.setAttribute("playerId", player.getId());
        session.setAttribute("username", player.getUsername());

        return "redirect:/menu";
    }

    // Affiche la page d'inscription
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // Traite le formulaire d'inscription
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           HttpSession session,
                           Model model) {

        boolean success = gameService.register(username, password);

        if (!success) {
            model.addAttribute("error", "Ce username est déjà pris !");
            return "register";
        }

        // Auto-login après inscription — stocke l'ID en session directement
        Player player = gameService.login(username, password);
        session.setAttribute("playerId", player.getId());
        session.setAttribute("username", player.getUsername());

        return "redirect:/menu";
    }

    // Déconnexion : supprime toutes les données de la session
    // Le joueur devra se reconnecter pour accéder au jeu
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // La racine "/" redirige directement vers la page de connexion
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
}