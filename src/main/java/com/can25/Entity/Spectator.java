package com.can25.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Spectator {
    @Id
    private String spectatorId;

    private int age;
    private String nationality;

    @Enumerated(EnumType.STRING)
    private BehaviorCategory category;

    private int totalMatches;

    // Champs transitoires (non sauvegardés dans la table SPECTATORS)
    @Transient
    private String matchId;

    @Transient
    private String entryTime;

    @Transient
    private String gate;

    @Transient
    private String ticketNumber;

    @Transient
    private String ticketType;

    @Transient
    private SeatLocation seatLocation;

}