package com.newsee.demo.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.newsee.demo.dto.CustomUserDetails;
import com.newsee.demo.entity.MemberEntity;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
	private final JWTUtil jwtUtil;
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		// request header 에서 토큰을 받아 옴.
		String authorization = request.getHeader("Authorization");

		// header 검증
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			System.out.println("token null");
			filterChain.doFilter(request, response);

			return ;
		}

		// Bearer 제거 후 순수 토큰 획득
		String token = authorization.split(" ")[1];
		// 토큰 시간 검증
		if(jwtUtil.isExpired(token)) {
			System.out.println("token expired");
			filterChain.doFilter(request, response);

			return ;
		}

		String username = jwtUtil.getUsername(token);
		String role = jwtUtil.getRole(token);
		Long id  = jwtUtil.getUserId(token);

		//Member entity 생성 set
		MemberEntity memberEntity = new MemberEntity();
		memberEntity.setId(id);
		memberEntity.setName(username);
		memberEntity.setPasswd("temppassword");
		memberEntity.setRole(role);

		//UserDetails 에 회원 정보 객체 담기
		CustomUserDetails customUserDetails = new CustomUserDetails(memberEntity);

		//스프링 시큐리티 인증 토큰 생성
		Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
		//세션에 사용자 등록
		SecurityContextHolder.getContext().setAuthentication(authToken);

		filterChain.doFilter(request, response);
	}
}
