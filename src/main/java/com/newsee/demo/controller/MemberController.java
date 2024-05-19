package com.newsee.demo.controller;

import com.newsee.demo.entity.MemberEntity;
import com.newsee.demo.entity.NewsEntity;
import com.newsee.demo.repository.MemberRepository;
import com.newsee.demo.request.MemberRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
@ResponseBody
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/join")
    public String createMember(@RequestBody MemberRequest request) {
        if (memberRepository.existsByName(request.getName())) { // 이름 중복 시 회원가입 진행X
            return "exist name";
        }

        // 새로운 멤버 생성
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setName(request.getName());
        memberEntity.setPasswd(bCryptPasswordEncoder.encode(request.getPasswd()));
        memberEntity.setStatus(true);
        memberEntity.setRole("ROLE_USER");
        memberRepository.save(memberEntity);

        return "ok";
    }
}
