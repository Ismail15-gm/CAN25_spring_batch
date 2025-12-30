/*Param :
*  spectatorId;
   age;
   nationality;
   matchId;
   entryTime;
   gate;
   ticketNumber;
   ticketType;
   seatLocation;
   category;
   totalMatches;
* */package com.can25.Entity;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Component
public class MapperDTO {


    public SpectatorDTO toDTO(Spectator spectator) {

        if (spectator == null) return null;

        return SpectatorDTO.builder()
                .spectatorId(spectator.getSpectatorId())
                .age(spectator.getAge())
                .nationality(spectator.getNationality())
                .matchId(spectator.getMatchId())
                .entryTime(parseLocalDateTime(spectator.getEntryTime()))
                .gate(spectator.getGate())
                .ticketNumber(spectator.getTicketNumber())
                .ticketType(spectator.getTicketType())
                .seatLocation(copySeatLocation(spectator.getSeatLocation()))
                .category(spectator.getCategory())
                .totalMatches(spectator.getTotalMatches())
                .build();
    }


    public Spectator toEntity(SpectatorDTO dto) {

        if (dto == null) return null;

        return Spectator.builder()
                .spectatorId(dto.getSpectatorId())
                .age(dto.getAge())
                .nationality(dto.getNationality())
                .matchId(dto.getMatchId())
                .entryTime(dto.getEntryTime() != null ? dto.getEntryTime().toString() : null)
                .gate(dto.getGate())
                .ticketNumber(dto.getTicketNumber())
                .ticketType(dto.getTicketType())
                .seatLocation(copySeatLocation(dto.getSeatLocation()))
                .category(dto.getCategory())
                .totalMatches(dto.getTotalMatches())
                .build();
    }

    private LocalDateTime parseLocalDateTime(String value) {
        if (value == null) return null;

        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            // fallback if format is not ISO
            try {
                return LocalDateTime.parse(value.replace(" ", "T"));
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private SeatLocation copySeatLocation(SeatLocation s) {
        if (s == null) return null;

        return SeatLocation.builder()
                .tribune(s.getTribune())
                .bloc(s.getBloc())
                .rang(s.getRang())
                .siege(s.getSiege())
                .build();
    }

}
