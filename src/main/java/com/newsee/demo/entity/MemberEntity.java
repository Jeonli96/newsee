package com.newsee.demo.entity;


import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Entity // 엔티티 정의
@EntityListeners(AuditingEntityListener.class)
@Getter // lombok getter
@Setter // lombok setter
@Table(name = "member")
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column
    private String name; // 이름

    @NonNull
    @Column
    private String passwd; // 비밀번호

    @NonNull
    @Column
    private String role; // 역할

    @NonNull
    @Column
    private Boolean status; // 탈퇴여부 

    @CreatedDate
    @Column(columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime createTime; // 등록일자
}

