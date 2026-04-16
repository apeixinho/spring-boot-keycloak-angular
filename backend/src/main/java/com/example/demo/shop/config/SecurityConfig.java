package com.example.demo.shop.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
						.requestMatchers("/h2-console/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/products/**", "/api/productcatalogs/**")
						.hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/orders/**")
						.hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/orders/**")
						.hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/orders/**")
						.hasAnyRole("USER", "ADMIN")
						.requestMatchers("/api/customers/**")
						.hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/products/**", "/api/productcatalogs/**")
						.hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/products/**", "/api/productcatalogs/**")
						.hasRole("ADMIN")
						.anyRequest()
						.authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

		return http.build();
	}

	@Bean
	public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
		return converter;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource(
			@Value("${app.security.allowed-origins:http://localhost:4200}") String allowedOrigins) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.filter(origin -> !origin.isBlank())
				.toList());
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	private static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
		@Override
		public Collection<GrantedAuthority> convert(Jwt jwt) {
			List<GrantedAuthority> authorities = new ArrayList<>();
			Object realmAccessObj = jwt.getClaim("realm_access");
			if (!(realmAccessObj instanceof Map<?, ?> realmAccess)) {
				return authorities;
			}

			Object rolesObj = realmAccess.get("roles");
			if (!(rolesObj instanceof Collection<?> roles)) {
				return authorities;
			}

			for (Object roleObj : roles) {
				if (roleObj instanceof String role && !role.isBlank()) {
					authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
				}
			}
			return authorities;
		}
	}
}
