package com.newsee.demo.controller;

import com.newsee.demo.entity.CommentEntity;
import com.newsee.demo.entity.NewsEntity;
import com.newsee.demo.jwt.JWTUtil;
import com.newsee.demo.repository.CommentRepository;
import com.newsee.demo.repository.NewsRepository;
import com.newsee.demo.request.CommentRequest;
import com.newsee.demo.response.NewsDetailResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {
    private final NewsRepository newsRepository;
    private final CommentRepository commentRepository;
    private final JWTUtil jwtUtil;

    //댓글 등록
    @PostMapping("/post")
    public NewsDetailResponse commentPost(@ModelAttribute CommentRequest request, HttpServletRequest httpServletRequest) {
        String token = jwtUtil.resolveToken(httpServletRequest);
        if (token == null || jwtUtil.isExpired(token)) {
            throw new RuntimeException("Comment Post Invalid JWT token");
        }
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);

        // 댓글 추가 로직
        CommentEntity commentEntity = new CommentEntity();
        // 클라이언트의 IP 주소를 가져옵니다.
        String clientIpAddress = httpServletRequest.getHeader("X-Forwarded-For");
        if (clientIpAddress == null || clientIpAddress.isEmpty()) {
            // 프록시 서버를 통해 요청이 전달되지 않은 경우, 클라이언트의 진짜 IP 주소는 getRemoteAddr() 메소드로 가져올 수 있습니다.
            clientIpAddress = httpServletRequest.getRemoteAddr();
        }
        commentEntity.setClientIP(clientIpAddress);
        commentEntity.setComments(request.getComment());
        commentEntity.setNewsId(request.getNewsId());
        commentEntity.setUserId(userId);
        commentEntity.setUserName(username);
        commentRepository.save(commentEntity);

        // 게시글 댓글 개수 증가 로직
        Optional<NewsEntity> newsDetailOptional = newsRepository.findById(request.getNewsId());
        NewsEntity newsDetail = newsDetailOptional.orElseThrow(() -> new RuntimeException("News not found"));
        newsDetail.setCommentCount(newsDetail.getCommentCount() + 1);
        newsRepository.save(newsDetail); // 변경된 댓글 개수를 저장

        List<CommentEntity> comments = commentRepository.findAllByNewsId(request.getNewsId());

        // 뉴스 및 댓글을 포함한 응답 생성
        NewsDetailResponse response = new NewsDetailResponse();
        response.setNews(newsDetail);
        response.setComments(comments);
        return response;
    }

    // 댓글 업데이트
    @PostMapping("/update")
    public NewsDetailResponse commentUpdate(@RequestBody CommentRequest request, HttpServletRequest httpServletRequest) {
        // 댓글 find
        Optional<CommentEntity> commentEntityOptional = commentRepository.findById(request.getId());
        CommentEntity commentDetail = commentEntityOptional.orElseThrow(() -> new RuntimeException("Comment not found"));

        // JWT 토큰 파싱
        String token = jwtUtil.resolveToken(httpServletRequest);
        if (token == null) {
            throw new RuntimeException("Comment Update Invalid JWT token");
        }
        // JWT 토큰에서 id 파싱
        Long userId = jwtUtil.getUserId(token);

        if (!userId.equals(commentDetail.getUserId())) {
            throw new RuntimeException("Comment Update Invalid id");
        }

        //업데이트 로직
        try {
            commentDetail.setComments(request.getComment());
            commentRepository.save(commentDetail); // 변경된 내용 저장
        } catch (Exception e) {
            throw new RuntimeException("Comment Update Exception = " + e);
        }

        Optional<NewsEntity> newsDetailOptional = newsRepository.findById(commentDetail.getNewsId());
        NewsEntity newsDetail = newsDetailOptional.orElseThrow(() -> new RuntimeException("News not found"));
        List<CommentEntity> comments = commentRepository.findAllByNewsId(commentDetail.getNewsId());

        // 뉴스 및 댓글을 포함한 응답 생성
        NewsDetailResponse response = new NewsDetailResponse();
        response.setNews(newsDetail);
        response.setComments(comments);
        return response;
    }

    // 댓글 삭제
    @GetMapping("/remove")
    public String commentRemove(@RequestParam(value = "id") long id, HttpServletRequest httpServletRequest) {
        // 댓글 find
        Optional<CommentEntity> commentEntityOptional = commentRepository.findById(id);
        CommentEntity commentDetail = commentEntityOptional.orElseThrow(() -> new RuntimeException("Comment not found"));

        // JWT 토큰 파싱
        String token = jwtUtil.resolveToken(httpServletRequest);
        if (token == null) {
            throw new RuntimeException("Comment Remove Invalid JWT token");
        }
        // JWT 토큰에서 id 파싱
        Long userId = jwtUtil.getUserId(token);

        if (!userId.equals(commentDetail.getUserId())) {
            throw new RuntimeException("Comment Remove Invalid id");
        }

        // 삭제 로직
        try {
            commentRepository.deleteById(id);
            return "News deleted successfully";
        } catch (Exception e) {
            throw new RuntimeException("Comment Remove Exception = " + e);
        }
    }
}
