package com.can25.Dto;

import com.can25.Entity.BehaviorCategory;
import com.can25.Entity.SeatLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpectatorDTO {
    // Informations spectateur
    private String spectatorId;
    private int age;
    private String nationality;

    // Informations d'entrée au match
    private String matchId;
    private LocalDateTime entryTime;
    private String gate;
    private String ticketNumber;
    private String ticketType;

    // Localisation
    private SeatLocation seatLocation;

    // Calculé par le processor
    private BehaviorCategory category;
    private int totalMatches;
}
