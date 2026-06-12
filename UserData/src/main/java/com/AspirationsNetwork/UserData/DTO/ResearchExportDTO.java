package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ResearchExportDTO {
    private String exportType;
    private Date generatedAt;
    private int recordCount;
    private List<Map<String, Object>> records = new ArrayList<>();
}
