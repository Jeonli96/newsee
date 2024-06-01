package com.newsee.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newsee.demo.entity.FileEntity;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
}
