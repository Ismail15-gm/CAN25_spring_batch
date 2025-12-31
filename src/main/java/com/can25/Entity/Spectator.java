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
public class Spectator {
    @Id
    private String spectatorId;

    private int age;
    private String nationality;

    @Enumerated(EnumType.STRING)
    private BehaviorCategory category;

    private int totalMatches;

}