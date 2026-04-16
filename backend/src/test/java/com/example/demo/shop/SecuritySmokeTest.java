package com.example.demo.shop;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "server.servlet.context-path=")
@AutoConfigureMockMvc
class SecuritySmokeTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void productsEndpointRejectsAnonymous() throws Exception {
		mockMvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
	}

	@Test
	void productsEndpointAllowsUserRole() throws Exception {
		mockMvc.perform(get("/api/products")
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
				.andExpect(status().isOk());
	}

	@Test
	void customersEndpointRequiresAdminRole() throws Exception {
		mockMvc.perform(get("/api/customers")
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
				.andExpect(status().isForbidden());
	}
}
