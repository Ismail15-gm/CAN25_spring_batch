package com.can25.Batch;

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

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DatabaseWriter implements ItemWriter<Spectator> {

    private final SpectatorRepository spectatorRepository;
    private final MatchEntryRepository matchEntryRepository;
    private final SpectatorStatisticsRepository spectatorStatisticsRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends Spectator> chunk) throws Exception {

        for (Spectator spectator : chunk) {


            Optional<Spectator> existingSpectatorOpt = spectatorRepository.findById(spectator.getSpectatorId());
            Spectator currentSpectator;

            if (existingSpectatorOpt.isEmpty()) {
                Spectator newSpectator = Spectator.builder()
                        .spectatorId(spectator.getSpectatorId())
                        .age(spectator.getAge())
                        .nationality(spectator.getNationality())
                        .totalMatches(spectator.getTotalMatches())
                        .category(spectator.getCategory())
                        .build();

                currentSpectator = spectatorRepository.save(newSpectator);
            } else {
                currentSpectator = existingSpectatorOpt.get();
                currentSpectator.setTotalMatches(spectator.getTotalMatches());
                currentSpectator.setCategory(spectator.getCategory());

                currentSpectator = spectatorRepository.save(currentSpectator);
            }


            MatchEntry entry = MatchEntry.builder()
                    .spectatorId(spectator.getSpectatorId())
                    .matchId(spectator.getMatchId())
                    .entryTime(LocalDateTime.parse(spectator.getEntryTime()))
                    .gate(spectator.getGate())
                    .ticketNumber(spectator.getTicketNumber())
                    .ticketType(spectator.getTicketType())
                    .seatLocation(spectator.getSeatLocation())
                    .build();

            matchEntryRepository.save(entry);


            Optional<SpectatorStatistics> statsOpt = spectatorStatisticsRepository.findBySpectatorId(currentSpectator);
            SpectatorStatistics stats;

            if (statsOpt.isEmpty()) {
                // Créer de nouvelles statistiques
                stats = SpectatorStatistics.builder()
                        .spectatorId(currentSpectator)
                        .totalMatches(spectator.getTotalMatches())
                        .behaviorCategory(spectator.getCategory().name())
                        .build();
            } else {
                // Mettre à jour les statistiques existantes
                stats = statsOpt.get();
                stats.setTotalMatches(spectator.getTotalMatches());
                stats.setBehaviorCategory(spectator.getCategory().name());
            }

            spectatorStatisticsRepository.save(stats);
        }
    }
}
