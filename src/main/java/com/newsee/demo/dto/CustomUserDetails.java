package com.newsee.demo.dto;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.core.annotation.Order;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.newsee.demo.entity.MemberEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
	private final MemberEntity memberEntity;
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> collection = new ArrayList<>();
		collection.add(new GrantedAuthority() {
			@Override
			public String getAuthority() {
				return memberEntity.getRole();
			}
		});
		return collection;
	}

	@Override
	public String getPassword() {
		return memberEntity.getPasswd();
	}

	@Override
	public String getUsername() { return memberEntity.getName();}

	public Long getUserId() { return memberEntity.getId(); }

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
