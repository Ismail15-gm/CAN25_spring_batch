package com.can25.Repository;

import com.can25.Entity.MatchEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchEntryRepository extends JpaRepository<MatchEntry, Long> {
}
