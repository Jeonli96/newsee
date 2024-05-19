package com.newsee.demo.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsee.demo.entity.CommentEntity;
import com.newsee.demo.entity.NewsEntity;
import com.newsee.demo.jwt.JWTUtil;
import com.newsee.demo.repository.CommentRepository;
import com.newsee.demo.repository.NewsRepository;
import com.newsee.demo.request.NewsRequest;
import com.newsee.demo.response.NewsDetailResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class NewsController {
	private final NewsRepository newsRepository;
	private final CommentRepository commentRepository;
	private final JWTUtil jwtUtil;

	// 게시글 등록
	@PostMapping("/newsPost")
	public NewsEntity newsPost(@RequestBody NewsRequest request, HttpServletRequest httpServletRequest) {
		// JWT 토큰 파싱
		String token = jwtUtil.resolveToken(httpServletRequest);
		if (token == null) {
			throw new RuntimeException("Invalid JWT token");
		}
		// JWT 토큰에서 id, 이름 파싱
		Long userId = jwtUtil.getUserId(token);
		String username = jwtUtil.getUsername(token);

		// 새로운 게시글 생성
		NewsEntity newsEntity = new NewsEntity();
		newsEntity.setTitle(request.getTitle());
		newsEntity.setContents(request.getContents());
		newsEntity.setViews(0);
		newsEntity.setCommentCount(0);
		newsEntity.setUserId(userId);
		newsEntity.setUserName(username);

		// 클라이언트의 IP 주소를 가져옵니다.
		String clientIpAddress = httpServletRequest.getHeader("X-Forwarded-For");
		if (clientIpAddress == null || clientIpAddress.isEmpty()) {
			// 프록시 서버를 통해 요청이 전달되지 않은 경우, 클라이언트의 진짜 IP 주소는 getRemoteAddr() 메소드로 가져올 수 있습니다.
			clientIpAddress = httpServletRequest.getRemoteAddr();
		}
		newsEntity.setClientIP(clientIpAddress);

		return newsRepository.save(newsEntity);
	}

	// 게시글 목록
	@GetMapping("/news")
	public Page<NewsEntity> newsList(@PageableDefault(size = 10) Pageable pageable) {
		// id를 내림차순으로 정렬하는 Sort 객체 생성
		Sort sort = Sort.by(Sort.Direction.DESC, "id");

		// Pageable 객체에 정렬 정보 추가
		Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

		// 정렬된 페이지네이션된 데이터 검색
		return newsRepository.findAll(sortedPageable);
	}

	// 게시글 상세보기
	@GetMapping("/detail")
	public NewsDetailResponse newsDetail(@RequestParam(value = "id") long id) {
		// 게시글 find
		Optional<NewsEntity> newsDetailOptional = newsRepository.findById(id);
		NewsEntity newsDetail = newsDetailOptional.orElseThrow(() -> new RuntimeException("News not found"));
		newsDetail.setViews(newsDetail.getViews() + 1); // 조회수 증가
		newsRepository.save(newsDetail); // 변경된 조회수를 저장

		// 댓글 find
		List<CommentEntity> comments = commentRepository.findAllByNewsId(id);

		// 뉴스 및 댓글을 포함한 응답 생성
		NewsDetailResponse response = new NewsDetailResponse();
		response.setNews(newsDetail);
		response.setComments(comments);

		return response;
	}

	// 게시글 업데이트
	@PostMapping("/newsUpdate")
	public NewsEntity newsUpdate(@RequestBody NewsRequest request, HttpServletRequest httpServletRequest) {
		// 게시글 find
		Optional<NewsEntity> newsDetailOptional = newsRepository.findById(request.getId());
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


		// 클라이언트의 IP 주소를 가져옵니다.
		String clientIpAddress = httpServletRequest.getHeader("X-Forwarded-For");
		if (clientIpAddress == null || clientIpAddress.isEmpty()) {
			// 프록시 서버를 통해 요청이 전달되지 않은 경우, 클라이언트의 진짜 IP 주소는 getRemoteAddr() 메소드로 가져올 수 있습니다.
			clientIpAddress = httpServletRequest.getRemoteAddr();
		}

		//업데이트 로직
		if (newsDetail.getClientIP().equals(clientIpAddress)) {
			newsDetail.setTitle(request.getTitle());
			newsDetail.setContents(request.getContents());
			return newsRepository.save(newsDetail); // 변경된 내용 저장
		}
		throw new RuntimeException("Unauthorized to update news");
	}

	// 게시글 삭제
	@GetMapping("/remove")
	public String newsRemove(@RequestParam(value = "id") long id, HttpServletRequest httpServletRequest) {
		// 게시글 find
		Optional<NewsEntity> newsDetailOptional = newsRepository.findById(id);
		NewsEntity newsDetail = newsDetailOptional.orElseThrow(() -> new RuntimeException("News not found"));

		//삭제 로직
		try {
			newsRepository.deleteById(id);
			return "News deleted successfully";
		} catch (Exception e) {
			throw new RuntimeException("Unauthorized to delete news = " + e);
		}
	}
}
