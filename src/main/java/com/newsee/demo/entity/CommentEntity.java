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
@Table(name = "comment")
public class CommentEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Long newsId;

	@NonNull
	@Column(length = 4000)
	private String comments; // 댓글

	@CreatedDate
	@Column(columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
	private LocalDateTime createTime; // 등록일자

	@NonNull
	@Column
	private String clientIP; // 댓글 단 IP

	@NonNull
	@Column
	private Long userId; // 유저 id

	@NonNull
	@Column
	private String userName; // 유저 이름
}
