package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ParticipantExternalLink {
    private String linkId;
    private String aspnParticipantId;
    private String userUID;
    private String externalDatasetId;
    private String externalSource;
    private String externalRecordId;
    private String externalDatasetName;
    private String linkStatus = "active";
    private Date linkedAt;
    private String linkedByStaffUID;
    private Date updatedAt;
    private Date removedAt;
    private String removedByStaffUID;
    private String notes;
}
