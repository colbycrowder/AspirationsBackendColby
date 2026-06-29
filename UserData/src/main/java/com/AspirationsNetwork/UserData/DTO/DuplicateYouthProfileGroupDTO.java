package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DuplicateYouthProfileGroupDTO {
    private String matchType;
    private String matchValue;
    private String reason;
    private List<DuplicateYouthProfileSummaryDTO> profiles = new ArrayList<>();
}
