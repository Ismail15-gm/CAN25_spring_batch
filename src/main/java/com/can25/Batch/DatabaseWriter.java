package com.can25.Batch;

import com.can25.Dto.SpectatorDTO;
import com.can25.Entity.MatchEntry;
import com.can25.Entity.Spectator;
import com.can25.Entity.SpectatorStatistics;
import com.can25.Repository.MatchEntryRepository;
import com.can25.Repository.SpectatorRepository;
import com.can25.Repository.SpectatorStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DatabaseWriter implements ItemWriter<SpectatorDTO> {

    private final SpectatorRepository spectatorRepository;
    private final MatchEntryRepository matchEntryRepository;
    private final SpectatorStatisticsRepository spectatorStatisticsRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends SpectatorDTO> chunk) throws Exception {

        for (SpectatorDTO dto : chunk) {

            // ----- 1. VERIFICATION ET MISE A JOUR DU SPECTATEUR -----
            // Vérifier si le spectateur existe déjà
            Optional<Spectator> existingSpectatorOpt = spectatorRepository.findById(dto.getSpectatorId());
            Spectator currentSpectator;

            if (existingSpectatorOpt.isEmpty()) {
                // Nouveau spectateur : on le persiste
                Spectator newSpectator = Spectator.builder()
                        .spectatorId(dto.getSpectatorId())
                        .age(dto.getAge())
                        .nationality(dto.getNationality())
                        .totalMatches(dto.getTotalMatches())
                        .category(dto.getCategory())
                        .build();

                currentSpectator = spectatorRepository.save(newSpectator);
            } else {
                // Spectateur existant : on met à jour
                currentSpectator = existingSpectatorOpt.get();
                currentSpectator.setTotalMatches(dto.getTotalMatches());
                currentSpectator.setCategory(dto.getCategory());

                currentSpectator = spectatorRepository.save(currentSpectator);
            }

            // ----- 2. CREATION DE L'ENTREE AU MATCH -----
            MatchEntry entry = MatchEntry.builder()
                    .spectator(currentSpectator) // Relation established
                    .matchId(dto.getMatchId())
                    .entryTime(dto.getEntryTime())
                    .gate(dto.getGate())
                    .ticketNumber(dto.getTicketNumber())
                    .ticketType(dto.getTicketType())
                    .seatLocation(dto.getSeatLocation())
                    .build();

            matchEntryRepository.save(entry);

            // ----- 3. MISE A JOUR DES STATISTIQUES -----
            // Vérifier si des stats existent pour ce spectateur
            Optional<SpectatorStatistics> statsOpt = spectatorStatisticsRepository.findBySpectatorId(currentSpectator);
            SpectatorStatistics stats;

            if (statsOpt.isEmpty()) {
                // Créer de nouvelles statistiques
                stats = SpectatorStatistics.builder()
                        .spectatorId(currentSpectator)
                        .totalMatches(dto.getTotalMatches())
                        .behaviorCategory(dto.getCategory().name())
                        .build();
            } else {
                // Mettre à jour les statistiques existantes
                stats = statsOpt.get();
                stats.setTotalMatches(dto.getTotalMatches());
                stats.setBehaviorCategory(dto.getCategory().name());
            }

            spectatorStatisticsRepository.save(stats);
        }
    }
}
