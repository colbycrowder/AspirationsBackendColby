package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTotalsDTO {
    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long youthUsers;
    private long staffUsers;
    private long educatorUsers;
    private long partnerUsers;
    private long governmentUsers;
}
