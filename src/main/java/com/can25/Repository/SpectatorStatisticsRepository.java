package com.can25.Repository;

import com.can25.Entity.Spectator;
import com.can25.Entity.SpectatorStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpectatorStatisticsRepository extends JpaRepository<SpectatorStatistics, Long> {
    Optional<SpectatorStatistics> findBySpectatorId(Spectator spectatorId);
}
