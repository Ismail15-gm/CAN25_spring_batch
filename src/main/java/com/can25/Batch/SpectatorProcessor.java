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

    private final Map<String, Integer> matchCount = new HashMap<>();

    @Override
    public SpectatorDTO process(SpectatorDTO spectator) {

        if (spectator.getSpectatorId() == null || spectator.getAge() <= 0) {
            return null;
        }

        matchCount.put(
                spectator.getSpectatorId(),
                matchCount.getOrDefault(spectator.getSpectatorId(), 0) + 1);
        int totalMatches = matchCount.get(spectator.getSpectatorId());
        spectator.setTotalMatches(totalMatches);


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
