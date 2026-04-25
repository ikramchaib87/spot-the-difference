package com.spotit.repository;

import com.spotit.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Player findByUsername(String username);
    boolean existsByUsername(String username);

    // ✅ Update direct en SQL — bypass Hibernate
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