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
public class MatchEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String matchId;
    private String spectatorId;
    private LocalDateTime entryTime;
    private String gate;
    private String ticketNumber;
    private String ticketType;
    @OneToOne(cascade = CascadeType.ALL)
    private SeatLocation seatLocation;
}
