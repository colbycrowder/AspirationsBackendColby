package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Program {
    private String programId;
    private String programName;
    private String description;
    private Date startDate;
    private Date endDate;
    private String category;
    private String programImageUrl;
    private String programLeader;
    private int capacity;
    private String programStatus = "active";
    private String createdByStaffUID;
    private Date createdAt;
    private Date updatedAt;
}
