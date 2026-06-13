package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class StakeholderRelationshipNoteTotalsDTO {
    private long totalNotes;
    private long activeNotes;
    private long inactiveNotes;
    private Map<String, Long> notesByStakeholderType = new HashMap<>();
    private Map<String, Long> notesByRelationshipStatus = new HashMap<>();
    private long upcomingFollowUps;
    private long overdueFollowUps;
}
