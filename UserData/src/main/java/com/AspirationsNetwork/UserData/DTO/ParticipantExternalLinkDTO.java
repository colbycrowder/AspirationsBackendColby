package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipantExternalLinkDTO {
    private String aspnParticipantId;
    private String externalDatasetId;
    private String externalRecordId;
    private String notes;
    private String linkedByStaffUID;
}
