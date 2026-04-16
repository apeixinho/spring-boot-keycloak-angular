package com.example.demo.shop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.example.demo.shop.exceptions.DoesNotExistException;
import com.example.demo.shop.model.CustomerInfo;
import com.example.demo.shop.service.CustomerServiceImpl;

class CustomerServiceBoundaryTest {

	private MockRestServiceServer server;
	private CustomerServiceImpl service;

	@BeforeEach
	void setUp() {
		RestTemplate restTemplate = new RestTemplate();
		server = MockRestServiceServer.bindTo(restTemplate).build();
		service = new CustomerServiceImpl(restTemplate);
		ReflectionTestUtils.setField(service, "keycloakAdminApiBaseUrl", "http://keycloak:8080/admin/realms/demo");
	}

	@Test
	void getCustomersForwardsBearerTokenAndParsesResponse() {
		Jwt jwt = Jwt.withTokenValue("demo-token").header("alg", "none").claim("sub", "admin").build();
		server.expect(requestTo("http://keycloak:8080/admin/realms/demo/users"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "Bearer demo-token"))
				.andRespond(withSuccess("[{\"username\":\"metalgear\"}]", MediaType.APPLICATION_JSON));

		List<CustomerInfo> customers = service.getCustomers(jwt);

		assertThat(customers).hasSize(1);
		assertThat(customers.get(0).getUsername()).isEqualTo("metalgear");
		server.verify();
	}

	@Test
	void getCustomerByUsernameThrowsWhenNoUsersReturned() {
		Jwt jwt = Jwt.withTokenValue("demo-token").header("alg", "none").claim("sub", "admin").build();
		server.expect(requestTo("http://keycloak:8080/admin/realms/demo/users?username=unknown"))
				.andExpect(method(GET))
				.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

		assertThatThrownBy(() -> service.getCustomerByUsername("unknown", jwt))
				.isInstanceOf(DoesNotExistException.class);
		server.verify();
	}
}
