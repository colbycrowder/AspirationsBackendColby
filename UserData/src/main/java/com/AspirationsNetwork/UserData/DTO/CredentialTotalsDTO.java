package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class CredentialTotalsDTO {
    private int totalDefinitions;
    private int activeDefinitions;
    private int archivedDefinitions;
    private int totalEarnedCredentials;
    private Map<String, Integer> definitionsByCategory = new HashMap<>();
    private Map<String, Integer> earnedCredentialsByCategory = new HashMap<>();
}
