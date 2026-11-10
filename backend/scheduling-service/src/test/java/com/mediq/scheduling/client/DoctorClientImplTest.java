package com.mediq.scheduling.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DoctorClientImplTest {

    private MockRestServiceServer mockServer;
    private DoctorClientImpl doctorClient;

    private static final String BASE_URL = "http://localhost:8081";

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        doctorClient = new DoctorClientImpl(builder, BASE_URL);
    }

    @Test
    @DisplayName("hasSpecialty returns true when doctor has target specialty")
    void hasSpecialty_Success() {
        UUID doctorId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();

        String jsonResponse = "[{\"specialtyId\":\"" + specialtyId + "\",\"isPrimary\":true}]";

        mockServer.expect(requestTo(BASE_URL + "/doctors/" + doctorId + "/specialties"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        boolean result = doctorClient.hasSpecialty(doctorId, specialtyId);

        assertThat(result).isTrue();
        mockServer.verify();
    }

    @Test
    @DisplayName("hasSpecialty returns false when doctor does not have target specialty")
    void hasSpecialty_NotMatched() {
        UUID doctorId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();
        UUID otherSpecialtyId = UUID.randomUUID();

        String jsonResponse = "[{\"specialtyId\":\"" + otherSpecialtyId + "\",\"isPrimary\":true}]";

        mockServer.expect(requestTo(BASE_URL + "/doctors/" + doctorId + "/specialties"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        boolean result = doctorClient.hasSpecialty(doctorId, specialtyId);

        assertThat(result).isFalse();
        mockServer.verify();
    }

    @Test
    @DisplayName("hasSpecialty returns false when remote returns 404 Not Found")
    void hasSpecialty_NotFound() {
        UUID doctorId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();

        mockServer.expect(requestTo(BASE_URL + "/doctors/" + doctorId + "/specialties"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        boolean result = doctorClient.hasSpecialty(doctorId, specialtyId);

        assertThat(result).isFalse();
        mockServer.verify();
    }

    @Test
    @DisplayName("hasSpecialty returns false when remote returns 500 Internal Server Error")
    void hasSpecialty_ServerError() {
        UUID doctorId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();

        mockServer.expect(requestTo(BASE_URL + "/doctors/" + doctorId + "/specialties"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        boolean result = doctorClient.hasSpecialty(doctorId, specialtyId);

        assertThat(result).isFalse();
        mockServer.verify();
    }

    @Test
    @DisplayName("getDoctor returns doctor DTO when doctor exists")
    void getDoctor_Success() {
        UUID doctorId = UUID.randomUUID();
        String jsonResponse = "{\"doctorId\":\"" + doctorId + "\",\"fullName\":\"Dr. Gregory House\",\"status\":\"ACTIVE\"}";

        mockServer.expect(requestTo(BASE_URL + "/doctors/" + doctorId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        Optional<DoctorClient.DoctorDto> result = doctorClient.getDoctor(doctorId);

        assertThat(result).isPresent();
        assertThat(result.get().doctorId()).isEqualTo(doctorId);
        assertThat(result.get().fullName()).isEqualTo("Dr. Gregory House");
        assertThat(result.get().status()).isEqualTo("ACTIVE");
        mockServer.verify();
    }

    @Test
    @DisplayName("getDoctor returns empty when remote returns 404")
    void getDoctor_NotFound() {
        UUID doctorId = UUID.randomUUID();

        mockServer.expect(requestTo(BASE_URL + "/doctors/" + doctorId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<DoctorClient.DoctorDto> result = doctorClient.getDoctor(doctorId);

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    @DisplayName("isDoctorActive returns true when status is ACTIVE")
    void isDoctorActive_True() {
        UUID doctorId = UUID.randomUUID();
        String jsonResponse = "{\"doctorId\":\"" + doctorId + "\",\"fullName\":\"Dr. House\",\"status\":\"ACTIVE\"}";

        mockServer.expect(requestTo(BASE_URL + "/doctors/" + doctorId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        boolean active = doctorClient.isDoctorActive(doctorId);

        assertThat(active).isTrue();
        mockServer.verify();
    }

    @Test
    @DisplayName("isDoctorActive returns false when status is INACTIVE or remote error")
    void isDoctorActive_False() {
        UUID doctorId = UUID.randomUUID();
        String jsonResponse = "{\"doctorId\":\"" + doctorId + "\",\"fullName\":\"Dr. House\",\"status\":\"INACTIVE\"}";

        mockServer.expect(requestTo(BASE_URL + "/doctors/" + doctorId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        boolean active = doctorClient.isDoctorActive(doctorId);

        assertThat(active).isFalse();
        mockServer.verify();
    }
}
