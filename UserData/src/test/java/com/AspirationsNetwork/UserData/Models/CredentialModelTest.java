package com.AspirationsNetwork.UserData.Models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CredentialModelTest {

    @Test
    void credentialDefinitionStartsInactiveAndDoesNotRequireHardcodedCatalogContent() {
        CredentialDefinition definition = new CredentialDefinition();
        definition.setCredentialID("credential-definition-123");

        assertEquals("credential-definition-123", definition.getCredentialID());
        assertFalse(definition.isActive());
    }

    @Test
    void earnedCredentialAssociatesUserWithCredentialDefinition() {
        EarnedCredential earnedCredential = new EarnedCredential();
        earnedCredential.setEarnedCredentialID("earned-credential-123");
        earnedCredential.setCredentialID("credential-definition-123");
        earnedCredential.setUserUID("user-123");
        earnedCredential.setAwardedByStaffUID("staff-123");

        assertEquals("earned-credential-123", earnedCredential.getEarnedCredentialID());
        assertEquals("credential-definition-123", earnedCredential.getCredentialID());
        assertEquals("user-123", earnedCredential.getUserUID());
        assertEquals("staff-123", earnedCredential.getAwardedByStaffUID());
        assertEquals("pending_review", earnedCredential.getStatus());
    }
}
