package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class User {
    private String uid;
    private String accountType;
    private String affiliation;
    private String [] badges;
    private String [] socials;
    private String firstName;
    private String lastName;
    private String email;
    private String avatar;
    private String profileImageUrl;
    private String role;
    private String aspnParticipantId;
    private Date aspnParticipantIdAssignedAt;
    private String aspnParticipantIdAssignedBy;
    private String aspnParticipantCohortYear;

    private boolean publicProfile = false;
    private boolean youthProfile = true;
    private String profileStatus = "pending_onboarding";
    private String school;
    private String graduationYear;
    private String desiredMajor;

    private List<String> programIds = new ArrayList<>();
    private List<String> programParticipationIds = new ArrayList<>();
    private List<String> credentialIds = new ArrayList<>();
    private List<String> earnedCredentialIds = new ArrayList<>();
    private List<String> attendanceRecordIds = new ArrayList<>();
    private List<String> serviceHourRecordIds = new ArrayList<>();
    private List<String> collegeInterests = new ArrayList<>();
    private List<String> careerInterests = new ArrayList<>();
    private List<String> desiredCareerFields = new ArrayList<>();
    private List<String> governmentCareerInterests = new ArrayList<>();
    private List<String> workforceInterests = new ArrayList<>();
    private List<String> civicInterests = new ArrayList<>();
    private List<String> communityInterests = new ArrayList<>();
    private List<String> publicServiceInterests = new ArrayList<>();

    private boolean staffReviewRequired = true;
    private boolean staffVerified = false;
    private boolean externalConsentReceived = false;
    private boolean credentialReviewAccess = false;
    private boolean attendanceReviewAccess = false;
    private boolean serviceHourVerificationAccess = false;
}
