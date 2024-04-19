package com.newsee.demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.newsee.demo.entity.CommentEntity;
import com.newsee.demo.entity.NewsEntity;
import com.newsee.demo.repository.CommentRepository;
import com.newsee.demo.repository.NewsRepository;
import com.newsee.demo.request.CommentRequest;
import com.newsee.demo.request.NewsRequest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NewsController {
	private final NewsRepository newsRepository;
	private final CommentRepository commentRepository;

	// 게시글 목록
	@GetMapping("/news")
	public String newsList(Model model, @PageableDefault(size = 10) Pageable pageable) {
		// id를 내림차순으로 정렬하는 Sort 객체 생성
		Sort sort = Sort.by(Sort.Direction.DESC, "id");

		// Pageable 객체에 정렬 정보 추가
		Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

		// 정렬된 페이지네이션된 데이터 검색
		Page<NewsEntity> newsPage = newsRepository.findAll(sortedPageable);
		model.addAttribute("newsPage", newsPage);
		return "news";
	}

	// 게시글 상세보기
	@GetMapping("/detail")
	public String newsDetail(Model model, @RequestParam(value = "id") long id) {
		// 게시글 find
		Optional<NewsEntity> newsDetailOptional = newsRepository.findById(id);
		NewsEntity newsDetail = newsDetailOptional.orElseThrow(() -> new RuntimeException("News not found"));
		newsDetail.setViews(newsDetail.getViews() + 1); // 조회수 증가
		newsRepository.save(newsDetail); // 변경된 조회수를 저장
		model.addAttribute("newsDetail", newsDetail);

		// 게시글 댓글 조회
		List<CommentEntity> commentDetail = commentRepository.findAllByNewsId(id);
		model.addAttribute("commentDetail", commentDetail);

		return "detail"; // detail.html 템플릿을 반환합니다.
	}

	@PostMapping("/newsPost")
	public String newsPost(@RequestBody NewsRequest request, HttpServletRequest httpServletRequest) {
		// 새로운 게시글 생성
		NewsEntity newsEntity = new NewsEntity();
		newsEntity.setTitle(request.getTitle());
		newsEntity.setContents(request.getContents());
		newsEntity.setViews(0);
		newsEntity.setCommentCount(0);

		// 클라이언트의 IP 주소를 가져옵니다.
		String clientIpAddress = httpServletRequest.getHeader("X-Forwarded-For");
		if (clientIpAddress == null || clientIpAddress.isEmpty()) {
			// 프록시 서버를 통해 요청이 전달되지 않은 경우, 클라이언트의 진짜 IP 주소는 getRemoteAddr() 메소드로 가져올 수 있습니다.
			clientIpAddress = httpServletRequest.getRemoteAddr();
		}
		newsEntity.setClientIP(clientIpAddress);

		newsRepository.save(newsEntity);

		// 게시글 목록 페이지로 리다이렉트
		return "redirect:/news";
	}

	@GetMapping("/remove")
	public String newsRemove(@RequestParam(value = "id") long id, HttpServletRequest httpServletRequest, Model model) {
		// 게시글 find
		Optional<NewsEntity> newsDetailOptional = newsRepository.findById(id);
		NewsEntity newsDetail = newsDetailOptional.orElseThrow(() -> new RuntimeException("News not found"));

		// 클라이언트의 IP 주소를 가져옵니다.
		String clientIpAddress = httpServletRequest.getHeader("X-Forwarded-For");
		if (clientIpAddress == null || clientIpAddress.isEmpty()) {
			// 프록시 서버를 통해 요청이 전달되지 않은 경우, 클라이언트의 진짜 IP 주소는 getRemoteAddr() 메소드로 가져올 수 있습니다.
			clientIpAddress = httpServletRequest.getRemoteAddr();
		}

		//삭제 로직
		if (newsDetail.getClientIP().equals(clientIpAddress)) {
			newsRepository.deleteById(id);
		}

		return "redirect:/news"; // news.html 템플릿을 반환합니다.
	}
}
