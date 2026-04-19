package com.spotit.repository;

import com.spotit.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

// ✅ JpaRepository<Player, Long> veut dire :
//    - on travaille avec l'entité Player
//    - l'id de Player est de type Long
// ✅ Spring génère automatiquement : save(), findById(), findAll(), delete()...
public interface PlayerRepository extends JpaRepository<Player, Long> {

    // ✅ Cherche un joueur par son username
    //    Spring comprend tout seul grâce au nom "findByUsername"
    Player findByUsername(String username);

    // ✅ Vérifie si un username existe déjà (pour l'inscription)
    boolean existsByUsername(String username);
}