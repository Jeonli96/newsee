package com.newsee.demo.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity // 엔티티 정의
@EntityListeners(AuditingEntityListener.class)
@Getter // lombok getter
@Setter // lombok setter
@Table(name = "files")
public class FileEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // 파일 id

	@Column(nullable = false)
	private String orgFilename; // 파일명

	@Column(nullable = false)
	private String filename; // 해쉬값으로 암호화하여 저장될 파일명

	@Column(nullable = false)
	private String filePath; // 저장될 경로

	@Lob
	private byte[] data;
}
