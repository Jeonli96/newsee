package com.newsee.demo.repository;

import com.newsee.demo.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository  extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByName(String name);

    Boolean existsByName(String name);
}
