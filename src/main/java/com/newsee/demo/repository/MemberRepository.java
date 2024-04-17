package com.newsee.demo.repository;

import com.newsee.demo.entity.MemberEntity;
import com.newsee.demo.entity.NewsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository  extends JpaRepository<MemberEntity, Long> {
    Optional<NewsEntity> findByName(String name);
}
