package com.can25.Batch;

import com.can25.Entity.MatchEntry;
import com.can25.Entity.Spectator;
import com.can25.Entity.SpectatorStatistics;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DatabaseWriter implements ItemWriter<Spectator> {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void write(Chunk<? extends Spectator> chunk) throws Exception {

        for (Spectator spectator : chunk) {

            // ----- 1. VERIFICATION ET MISE A JOUR DU SPECTATEUR -----
            // Vérifier si le spectateur existe déjà
            Spectator existingSpectator = em.find(Spectator.class, spectator.getSpectatorId());

            if (existingSpectator == null) {
                // Nouveau spectateur : on le persiste
                Spectator newSpectator = Spectator.builder()
                        .spectatorId(spectator.getSpectatorId())
                        .age(spectator.getAge())
                        .nationality(spectator.getNationality())
                        .totalMatches(spectator.getTotalMatches())
                        .category(spectator.getCategory())
                        .build();

                em.persist(newSpectator);
            } else {
                // Spectateur existant : on met à jour
                existingSpectator.setTotalMatches(spectator.getTotalMatches());
                existingSpectator.setCategory(spectator.getCategory());

                em.merge(existingSpectator);
            }

            // ----- 2. CREATION DE L'ENTREE AU MATCH -----
            MatchEntry entry = MatchEntry.builder()
                    .spectatorId(spectator.getSpectatorId())
                    .matchId(spectator.getMatchId())  // Depuis les champs @Transient
                    .entryTime(LocalDateTime.parse(spectator.getEntryTime()))
                    .gate(spectator.getGate())
                    .ticketNumber(spectator.getTicketNumber())
                    .ticketType(spectator.getTicketType())
                    .seatLocation(spectator.getSeatLocation())
                    .build();

            em.persist(entry);

            // ----- 3. MISE A JOUR DES STATISTIQUES -----
            // Vérifier si des stats existent pour ce spectateur
            SpectatorStatistics stats = em.createQuery(
                            "SELECT s FROM SpectatorStatistics s WHERE s.spectatorId.spectatorId = :spectatorId",
                            SpectatorStatistics.class)
                    .setParameter("spectatorId", spectator.getSpectatorId())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (stats == null) {
                // Créer de nouvelles statistiques
                stats = SpectatorStatistics.builder()
                        .spectatorId(existingSpectator) // A khoya azedinne ach hadchi
                        .totalMatches(spectator.getTotalMatches())
                        .behaviorCategory(spectator.getCategory().name())
                        .build();

                em.persist(stats);
            } else {
                // Mettre à jour les statistiques existantes
                stats.setTotalMatches(spectator.getTotalMatches());
                stats.setBehaviorCategory(spectator.getCategory().name());

                em.merge(stats);
            }
        }
    }
}

