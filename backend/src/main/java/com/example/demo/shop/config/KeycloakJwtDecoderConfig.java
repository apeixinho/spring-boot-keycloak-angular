package com.example.demo.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class KeycloakJwtDecoderConfig {

	@Bean
	public JwtDecoder jwtDecoder(
			@Value("${app.keycloak.jwks-uri}") String jwkSetUri,
			@Value("${app.keycloak.issuer-uri}") String issuerUri) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
		return decoder;
	}
}
