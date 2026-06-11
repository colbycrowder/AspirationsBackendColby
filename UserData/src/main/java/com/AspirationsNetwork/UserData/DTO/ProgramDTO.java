package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ProgramDTO {
    private String programName;
    private String description;
    private Date startDate;
    private Date endDate;
    private String category;
    private String programImageUrl;
    private String programLeader;
    private Integer capacity;
    private String programStatus;
    private String createdByStaffUID;
}
