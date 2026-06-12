package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalDatasetDTO {
    private String externalDatasetId;
    private String datasetName;
    private String externalSource;
    private String description;
    private String collectionPurpose;
    private Boolean containsPII;
    private Boolean active;
    private String createdByStaffUID;
}
