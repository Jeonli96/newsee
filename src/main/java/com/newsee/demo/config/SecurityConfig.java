package com.newsee.demo.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.newsee.demo.jwt.JWTFilter;
import com.newsee.demo.jwt.JWTUtil;
import com.newsee.demo.jwt.LoginFilter;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	// AuthenticationManager 가 인자로 받을 AuthenticationConfiguration 객체 생성
	private final AuthenticationConfiguration authenticationConfiguration;
	private final JWTUtil jwtUtil;

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
		return configuration.getAuthenticationManager();
	}

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		// CORS 설정
		http.
			cors((cors) -> cors
				.configurationSource(new CorsConfigurationSource() {
					@Override
					public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
						CorsConfiguration configuration = new CorsConfiguration();

						configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
						configuration.setAllowedMethods(Collections.singletonList("*"));
						configuration.setAllowCredentials(true);
						configuration.setAllowedHeaders(Collections.singletonList("*"));
						configuration.setMaxAge(3600L);
						// Authorization 헤더 허용
						configuration.setExposedHeaders(Collections.singletonList("Authorization"));
						return configuration;
					}
				}));

		//csrf disable
		http.csrf((auth) -> auth.disable());

		//form login disable
		http.formLogin((auth) -> auth.disable());

		//http basic 인증 방식 disable
		http.httpBasic((auth) -> auth.disable());

		//경로별 인가 작업
		http.authorizeHttpRequests(
			(auth) -> auth.requestMatchers("/login", "/", "join", "/api/file/*").permitAll()
				.requestMatchers("/admin").hasRole("ADMIN")
				.anyRequest().authenticated()
		);

		//JWTFilter 등록
		http
			.addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);

		// 로그인 필터 추가
		http
			.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);

		//세션 설정 (JWT를 통한 인증/인가를 위해 세션을 stateless 로 설정)
		http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}
}
