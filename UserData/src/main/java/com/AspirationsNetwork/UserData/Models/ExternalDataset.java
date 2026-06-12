package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ExternalDataset {
    private String externalDatasetId;
    private String datasetName;
    private String externalSource;
    private String description;
    private String collectionPurpose;
    private boolean containsPII;
    private boolean active = true;
    private String createdByStaffUID;
    private Date createdAt;
    private Date updatedAt;
}
