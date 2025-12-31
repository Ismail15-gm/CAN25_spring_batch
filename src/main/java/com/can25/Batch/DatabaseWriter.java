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

    /**
     * WRITER LOGIC
     * Using @Transactional ensures that for each chunk (e.g., 10 items), either ALL
     * updates succeed
     * or NONE do. If an error happens midway, the data is rolled back.
     */
    @Override
    @Transactional
    public void write(Chunk<? extends SpectatorDTO> chunk) throws Exception {

        for (SpectatorDTO dto : chunk) {

            // ----- 1. VERIFICATION ET MISE A JOUR DU SPECTATEUR (Entity: Spectator) -----
            // We check if we already know this spectator.
            // If yes -> Update their stats.
            // If no -> Create a new Spectator record.
            Optional<Spectator> existingSpectatorOpt = spectatorRepository.findById(dto.getSpectatorId());
            Spectator currentSpectator;

            if (existingSpectatorOpt.isEmpty()) {
                // NEW SPECTATOR
                Spectator newSpectator = Spectator.builder()
                        .spectatorId(dto.getSpectatorId())
                        .age(dto.getAge())
                        .nationality(dto.getNationality())
                        .totalMatches(dto.getTotalMatches())
                        .category(dto.getCategory())
                        .build();

                currentSpectator = spectatorRepository.save(newSpectator);
            } else {
                // EXISTING SPECTATOR (Update info)
                currentSpectator = existingSpectatorOpt.get();
                currentSpectator.setTotalMatches(dto.getTotalMatches());
                currentSpectator.setCategory(dto.getCategory());

                currentSpectator = spectatorRepository.save(currentSpectator);
            }

            // ----- 2. creation DE L'ENTREE AU MATCH (Entity: MatchEntry) -----
            // This table logs the specific event: "Spectator X entered Match Y at Time Z"
            MatchEntry entry = MatchEntry.builder()
                    .spectator(currentSpectator) // Link to the parent Spectator entity
                    .matchId(dto.getMatchId())
                    .entryTime(dto.getEntryTime())
                    .gate(dto.getGate())
                    .ticketNumber(dto.getTicketNumber())
                    .ticketType(dto.getTicketType())
                    .seatLocation(dto.getSeatLocation())
                    .build();

            matchEntryRepository.save(entry);

            // ----- 3. MISE A JOUR DES STATISTIQUES (Entity: SpectatorStatistics) -----
            // Maintain a separate summary table for easy reporting
            Optional<SpectatorStatistics> statsOpt = spectatorStatisticsRepository.findBySpectatorId(currentSpectator);
            SpectatorStatistics stats;

            if (statsOpt.isEmpty()) {
                // Create new stats record
                stats = SpectatorStatistics.builder()
                        .spectatorId(currentSpectator)
                        .totalMatches(dto.getTotalMatches())
                        .behaviorCategory(dto.getCategory().name())
                        .build();
            } else {
                // Update existing stats
                stats = statsOpt.get();
                stats.setTotalMatches(dto.getTotalMatches());
                stats.setBehaviorCategory(dto.getCategory().name());
            }

            spectatorStatisticsRepository.save(stats);
        }
    }
}
