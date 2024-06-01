package com.newsee.demo.controller;

import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.newsee.demo.entity.FileEntity;
import com.newsee.demo.entity.NewsEntity;
import com.newsee.demo.repository.FileRepository;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {
	private final FileRepository fileRepository;

	@GetMapping("/download/{id}")
	public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
		Optional<FileEntity> fileEntityOptional = fileRepository.findById(id);
		FileEntity fileEntity = fileEntityOptional.orElseThrow(() -> new RuntimeException("File not found"));

		String orgFilename = fileEntity.getOrgFilename();
		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType(orgFilename.substring(orgFilename.lastIndexOf(".") + 1)))
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getOrgFilename() + "\"")
			.body(fileEntity.getData());
	}
}
