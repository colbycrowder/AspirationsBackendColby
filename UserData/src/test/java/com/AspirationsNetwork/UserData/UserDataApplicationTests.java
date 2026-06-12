package com.AspirationsNetwork.UserData;

import com.AspirationsNetwork.UserData.Controller.UserInfoController;
import com.AspirationsNetwork.UserData.Service.AttendanceService;
import com.AspirationsNetwork.UserData.Service.AuthService;
import com.AspirationsNetwork.UserData.Service.CredentialService;
import com.AspirationsNetwork.UserData.Service.DashboardService;
import com.AspirationsNetwork.UserData.Service.DiscussionPostService;
import com.AspirationsNetwork.UserData.Service.MetricsService;
import com.AspirationsNetwork.UserData.Service.NotificationService;
import com.AspirationsNetwork.UserData.Service.ParticipantIdService;
import com.AspirationsNetwork.UserData.Service.PlatformEventService;
import com.AspirationsNetwork.UserData.Service.ProgramEnrollmentService;
import com.AspirationsNetwork.UserData.Service.ProgramService;
import com.AspirationsNetwork.UserData.Service.RwdLearningService;
import com.AspirationsNetwork.UserData.Service.ServiceHourService;
import com.AspirationsNetwork.UserData.Service.SystemSettingsService;
import com.AspirationsNetwork.UserData.Service.UserInfoService;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootTest(
		classes = {
				UserDataApplicationTests.TestApplication.class,
				UserDataApplicationTests.FirebaseTestConfig.class
		},
		properties = {
				"spring.cloud.gcp.firestore.enabled=false",
				"spring.cloud.gcp.storage.enabled=false"
		}
)
class UserDataApplicationTests {

	@Test
	void contextLoads() {
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import({
			UserInfoController.class,
			AttendanceService.class,
			AuthService.class,
			CredentialService.class,
			DashboardService.class,
			MetricsService.class,
			NotificationService.class,
			ParticipantIdService.class,
			PlatformEventService.class,
			ProgramEnrollmentService.class,
			ProgramService.class,
			RwdLearningService.class,
			ServiceHourService.class,
			SystemSettingsService.class,
			UserInfoService.class,
			DiscussionPostService.class
	})
	static class TestApplication {
	}

	@TestConfiguration
	static class FirebaseTestConfig {

		@Bean
		FirebaseApp firebaseApp() {
			return Mockito.mock(FirebaseApp.class);
		}

		@Bean
		Firestore firestore() {
			return Mockito.mock(Firestore.class);
		}

		@Bean
		FirebaseAuth firebaseAuth() {
			return Mockito.mock(FirebaseAuth.class);
		}
	}

}
