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
