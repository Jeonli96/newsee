package com.newsee.demo.controller;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.newsee.demo.dto.CustomUserDetails;

@Controller
@ResponseBody
public class MainController {
	@GetMapping("/")
	public String MainPage() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// CustomUserDetails 객체에서 id, name, role 가져오기
		if (authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

			Long id = userDetails.getUserId();
			String name = userDetails.getUsername();
			Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
			Iterator<? extends GrantedAuthority> iter = authorities.iterator();
			String role = "";
			if (iter.hasNext()) {
				GrantedAuthority auth = iter.next();
				role = auth.getAuthority();
			}

			return "Main id = " + id + ", name = " + name + ", role = " + role;
		} else {
			// 예외 처리 또는 기본 값 반환
			return "User not authenticated";
		}
	}
}
