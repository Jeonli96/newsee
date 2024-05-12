package com.newsee.demo.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.newsee.demo.dto.CustomUserDetails;
import com.newsee.demo.entity.MemberEntity;
import com.newsee.demo.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	private final MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		MemberEntity memberData = memberRepository.findByName(username);
		if (memberData != null) {
			return new CustomUserDetails(memberData);
		}

		return null;
	}
}
