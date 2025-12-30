package com.can25.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpectatorStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long statsId ;
    @ManyToOne(fetch = FetchType.EAGER)
    private Spectator spectatorId  ;
    private String behaviorCategory  ;
    private Integer totalMatches;



}
