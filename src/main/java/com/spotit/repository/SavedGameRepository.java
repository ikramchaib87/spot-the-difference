package com.spotit.repository;

import com.spotit.model.Player;
import com.spotit.model.SavedGame;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedGameRepository extends JpaRepository<SavedGame, Long> {

    // ✅ Cherche la partie sauvegardée d'un joueur
    //    Un joueur ne peut avoir qu'une seule sauvegarde à la fois
    SavedGame findByPlayer(Player player);

    // ✅ Vérifie si le joueur a déjà une partie sauvegardée
    boolean existsByPlayer(Player player);
}