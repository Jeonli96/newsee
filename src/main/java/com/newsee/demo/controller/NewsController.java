package com.newsee.demo.controller;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsee.demo.entity.CommentEntity;
import com.newsee.demo.entity.FileEntity;
import com.newsee.demo.entity.NewsEntity;
import com.newsee.demo.jwt.JWTUtil;
import com.newsee.demo.repository.CommentRepository;
import com.newsee.demo.repository.FileRepository;
import com.newsee.demo.repository.NewsRepository;
import com.newsee.demo.request.NewsRequest;
import com.newsee.demo.response.NewsDetailResponse;
import com.newsee.demo.util.MD5Generator;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {
	private final NewsRepository newsRepository;
	private final CommentRepository commentRepository;
	private final FileRepository fileRepository;
	private final JWTUtil jwtUtil;

	// 게시글 등록
	@PostMapping("/post")
	public NewsEntity newsPost(@RequestParam("file") MultipartFile files, @RequestParam("request") String requestJson, HttpServletRequest httpServletRequest) {
		// JWT 토큰 파싱
		String token = jwtUtil.resolveToken(httpServletRequest);
		if (token == null) {
			throw new RuntimeException("News Post Invalid JWT token");
		}
		// JWT 토큰에서 id, 이름 파싱
		Long userId = jwtUtil.getUserId(token);
		String username = jwtUtil.getUsername(token);

		// request 를 json 으로 파싱 후 NewsRequest 로 반환
		ObjectMapper objectMapper = new ObjectMapper();
		NewsRequest request;
		try {
			request = objectMapper.readValue(requestJson, NewsRequest.class);
		} catch (Exception e) {
			throw new RuntimeException("Invalid JSON format", e);
		}

		// 새로운 게시글 생성
		NewsEntity newsEntity = new NewsEntity();
		newsEntity.setTitle(request.getTitle());
		newsEntity.setContents(request.getContents());
		newsEntity.setViews(0);
		newsEntity.setCommentCount(0);
		newsEntity.setUserId(userId);
		newsEntity.setUserName(username);

		//파일 업로드
		if (files == null || files.isEmpty()) {
			newsEntity.setFileId(null);
		} else {
			try {
				String orgFileName = files.getOriginalFilename();
				String filename = new MD5Generator(orgFileName).toString();
				String savePath = System.getProperty("user.dir") + File.separator + "files";
				if (!new File(savePath).exists()) {
					try {
						new File(savePath).mkdir();
					} catch (Exception e) {
						e.getStackTrace();
					}
				}
				String filePath = savePath + File.separator + filename;
				files.transferTo(new File(filePath));

				FileEntity fileEntity = new FileEntity();
				fileEntity.setFilename(filename);
				fileEntity.setFilePath(filePath);
				fileEntity.setOrgFilename(orgFileName);
				fileEntity.setData(files.getBytes());
				Long fileId = fileRepository.save(fileEntity).getId();
				newsEntity.setFileId(fileId);
			} catch (Exception e) {
				throw new RuntimeException("News Post File Upload Exception = " + e);
			}
		}

		// 클라이언트의 IP 주소를 가져옵니다.
		String clientIpAddress = httpServletRequest.getHeader("X-Forwarded-For");
		if (clientIpAddress == null || clientIpAddress.isEmpty()) {
			// 프록시 서버를 통해 요청이 전달되지 않은 경우, 클라이언트의 진짜 IP 주소는 getRemoteAddr() 메소드로 가져올 수 있습니다.
			clientIpAddress = httpServletRequest.getRemoteAddr();
		}
		newsEntity.setClientIP(clientIpAddress);

		try {
			return newsRepository.save(newsEntity);
		} catch (Exception e) {
			throw new RuntimeException("News Post Exception = " + e);
		}
	}

	// 게시글 목록
	@GetMapping("/news")
	public Page<NewsEntity> newsList(@PageableDefault(size = 10) Pageable pageable) {
		// id를 내림차순으로 정렬하는 Sort 객체 생성
		Sort sort = Sort.by(Sort.Direction.DESC, "id");

		// Pageable 객체에 정렬 정보 추가
		Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

		// 정렬된 페이지네이션된 데이터 검색
		try {
			return newsRepository.findAll(sortedPageable);
		} catch (Exception e) {
			throw new RuntimeException("News List Exception = " + e);
		}
	}

	// 게시글 상세보기
	@GetMapping("/detail")
	public NewsDetailResponse newsDetail(@RequestParam(value = "id") long id) {
		// 게시글 find
		Optional<NewsEntity> newsDetailOptional = newsRepository.findById(id);
		NewsEntity newsDetail = newsDetailOptional.orElseThrow(() -> new RuntimeException("News Detail not found"));
		newsDetail.setViews(newsDetail.getViews() + 1); // 조회수 증가
		newsRepository.save(newsDetail); // 변경된 조회수를 저장

		// 댓글 find
		List<CommentEntity> comments = commentRepository.findAllByNewsId(id);

		// 뉴스 및 댓글을 포함한 응답 생성
		NewsDetailResponse response = new NewsDetailResponse();
		response.setNews(newsDetail);
		response.setComments(comments);

		try {
			return response;
		} catch (Exception e) {
			throw new RuntimeException("News Detail Exception = " + e);
		}
	}

	// 게시글 업데이트
	@PostMapping("/update")
	public NewsEntity newsUpdate(@RequestBody NewsRequest request, HttpServletRequest httpServletRequest) {
		// 게시글 find
		Optional<NewsEntity> newsDetailOptional = newsRepository.findById(request.getId());
		NewsEntity newsDetail = newsDetailOptional.orElseThrow(() -> new RuntimeException("News Update News not found"));

		// JWT 토큰 파싱
		String token = jwtUtil.resolveToken(httpServletRequest);
		if (token == null) {
			throw new RuntimeException("News Update Invalid JWT token");
		}
		// JWT 토큰에서 id 파싱
		Long userId = jwtUtil.getUserId(token);

		if (!userId.equals(newsDetail.getUserId())) {
			throw new RuntimeException("News Update Invalid id");
		}

		//업데이트 로직
		try {
			newsDetail.setTitle(request.getTitle());
			newsDetail.setContents(request.getContents());
			return newsRepository.save(newsDetail); // 변경된 내용 저장
		} catch (Exception e) {
			throw new RuntimeException("News Update Exception = " + e);
		}
	}

	// 게시글 삭제
	@GetMapping("/remove")
	public String newsRemove(@RequestParam(value = "id") long id, HttpServletRequest httpServletRequest) {
		// 게시글 find
		Optional<NewsEntity> newsDetailOptional = newsRepository.findById(id);
		NewsEntity newsDetail = newsDetailOptional.orElseThrow(() -> new RuntimeException("News not found"));

		// JWT 토큰 파싱
		String token = jwtUtil.resolveToken(httpServletRequest);
		if (token == null) {
			throw new RuntimeException("Invalid JWT token");
		}
		// JWT 토큰에서 id 파싱
		Long userId = jwtUtil.getUserId(token);
		if (!userId.equals(newsDetail.getUserId())) {
			throw new RuntimeException("Invalid id");
		}

		//삭제 로직
		try {
			newsRepository.deleteById(id);
			return "News deleted successfully";
		} catch (Exception e) {
			throw new RuntimeException("Unauthorized to delete news = " + e);
		}
	}
}
