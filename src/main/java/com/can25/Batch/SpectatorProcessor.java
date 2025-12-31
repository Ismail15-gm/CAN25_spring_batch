package com.can25.Batch;

import com.can25.Dto.MapperDTO;
import com.can25.Dto.SpectatorDTO;
import com.can25.Entity.BehaviorCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SpectatorProcessor implements ItemProcessor<SpectatorDTO, SpectatorDTO> {

    // Simple in-memory tracking of spectator visits.
    // NOTE: In a real-world distributed batch (multiple servers), this would need
    // to be stored
    // in a database or distributed cache (like Redis) so all nodes share the count.
    // For this single-instance batch, a HashMap is sufficient.
    private final Map<String, Integer> matchCount = new HashMap<>();

    /**
     * PROCESSOR LOGIC
     * This method contains the "Business Logic" of our batch job.
     * It transforms the input data (SpectatorDTO) before writing it.
     */
    @Override
    public SpectatorDTO process(SpectatorDTO spectator) {

        // 1. VALIDATION
        // Filter out invalid records. Returning 'null' in an ItemProcessor tells Spring
        // Batch
        // to filter out (skip) this item. It will NOT be sent to the writer.
        if (spectator.getSpectatorId() == null || spectator.getAge() <= 0) {
            return null;
        }

        // 2. STATE MANAGEMENT (Calculate Total Visits)
        // Update the visit count for this spectator
        matchCount.put(
                spectator.getSpectatorId(),
                matchCount.getOrDefault(spectator.getSpectatorId(), 0) + 1);

        int totalMatches = matchCount.get(spectator.getSpectatorId());
        spectator.setTotalMatches(totalMatches);

        // 3. CATEGORIZATION (Business Rule)
        // Assign a category based on the calculated total matches.
        if (totalMatches == 1)
            spectator.setCategory(BehaviorCategory.PREMIERE_VISITE);
        else if (totalMatches <= 3)
            spectator.setCategory(BehaviorCategory.OCCASIONNEL);
        else if (totalMatches <= 6)
            spectator.setCategory(BehaviorCategory.REGULIER);
        else
            spectator.setCategory(BehaviorCategory.SUPER_FAN);

        return spectator;
    }
}
