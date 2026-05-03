package com.spotit.repository;

import com.spotit.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Spring génère automatiquement les requêtes SQL à partir des noms des méthodes
// Pas besoin d'écrire de SQL pour les opérations de base (save, findById, delete...)
public interface PlayerRepository extends JpaRepository<Player, Long> {

    // Spring génère automatiquement : SELECT * FROM player WHERE username = ?
    Player findByUsername(String username);

    // Spring génère automatiquement : SELECT COUNT(*) FROM player WHERE username = ?
    // Utilisé lors de l'inscription pour vérifier si le username est déjà pris
    boolean existsByUsername(String username);

    // Requête JPQL directe pour mettre à jour les scores du joueur
    // On utilise @Modifying car les objets Player récupérés depuis la session HTTP
    // sont "détachés" de la session JPA — Hibernate ne peut pas les mettre à jour
    // via save(). Cette approche bypasse ce problème en écrivant directement en BDD.
    @Modifying
    @Query("UPDATE Player p SET " +
           "p.scoreLevel1 = :s1, p.scoreLevel2 = :s2, p.scoreLevel3 = :s3, " +
           "p.scoreLevel4 = :s4, p.scoreLevel5 = :s5, " +
           "p.bestScore = :best, p.unlockedLevel = :lvl " +
           "WHERE p.id = :id")
    void updatePlayerStats(@Param("id") Long id,
                           @Param("s1") int s1, @Param("s2") int s2,
                           @Param("s3") int s3, @Param("s4") int s4,
                           @Param("s5") int s5,
                           @Param("best") int best,
                           @Param("lvl") int lvl);
}