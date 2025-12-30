package com.can25.Batch;

import com.can25.Entity.BehaviorCategory;
import com.can25.Entity.MapperDTO;
import com.can25.Entity.Spectator;
import com.can25.Entity.SpectatorDTO;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
public class SpectatorProcessor implements ItemProcessor<SpectatorDTO, Spectator> {

    private final MapperDTO mapperDTO;

    private final Map<String, Integer> matchCount = new HashMap<>();

    public SpectatorProcessor(MapperDTO mapperDTO) {
        this.mapperDTO = mapperDTO;
    }

    @Override
    public Spectator process(SpectatorDTO spectator) {

        // ----- VALIDATION -----
        if (spectator.getSpectatorId() == null || spectator.getAge() <= 0) {
            return null;
        }

        // ----- CALCUL DU NOMBRE DE MATCHS -----
        matchCount.put(
                spectator.getSpectatorId(),
                matchCount.getOrDefault(spectator.getSpectatorId(), 0) + 1
        );
        int totalMatches = matchCount.get(spectator.getSpectatorId());
        spectator.setTotalMatches(totalMatches);

        // ----- CLASSIFICATION -----
        if (totalMatches == 1) spectator.setCategory(BehaviorCategory.PREMIERE_VISITE);
        else if (totalMatches <= 3) spectator.setCategory(BehaviorCategory.OCCASIONNEL);
        else if (totalMatches <= 6) spectator.setCategory(BehaviorCategory.REGULIER);
        else spectator.setCategory(BehaviorCategory.SUPER_FAN);

        return mapperDTO.toEntity(spectator);
    }
}
