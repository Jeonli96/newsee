package com.newsee.demo.controller;

import com.newsee.demo.entity.MemberEntity;
import com.newsee.demo.entity.NewsEntity;
import com.newsee.demo.repository.MemberRepository;
import com.newsee.demo.request.MemberRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;

    @PostMapping("/member")
    public String createMember(@RequestBody MemberRequest request) {
        // 새로운 멤버 생성
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setName(request.getName());
        memberEntity.setPasswd(request.getPasswd());
        memberEntity.setNickname(request.getNickname());
        memberEntity.setStatus(true);
        memberRepository.save(memberEntity);

        return "news";
    }
}
