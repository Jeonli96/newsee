package com.newsee.demo.controller;

import com.newsee.demo.entity.CommentEntity;
import com.newsee.demo.entity.NewsEntity;
import com.newsee.demo.repository.CommentRepository;
import com.newsee.demo.repository.NewsRepository;
import com.newsee.demo.request.CommentRequest;
import com.newsee.demo.request.NewsRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class CommentController {
    private final NewsRepository newsRepository;
    private final CommentRepository commentRepository;

    //댓글 등록
    @PostMapping("/commentPost")
    public String commentPost(@ModelAttribute CommentRequest request, HttpServletRequest httpServletRequest) {
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
        commentRepository.save(commentEntity);

        // 게시글 댓글 개수 증가 로직
        Optional<NewsEntity> newsDetailOptional = newsRepository.findById(request.getNewsId());
        NewsEntity newsDetail = newsDetailOptional.orElseThrow(() -> new RuntimeException("News not found"));
        newsDetail.setCommentCount(newsDetail.getCommentCount() + 1);
        newsRepository.save(newsDetail); // 변경된 댓글 개수를 저장

        return "redirect:/detail?id=" + request.getNewsId();
    }

    // 댓글 업데이트
    @PostMapping("/commentUpdate")
    public String commentUpdate(@RequestBody CommentRequest request, HttpServletRequest httpServletRequest) {
        // 댓글 find
        Optional<CommentEntity> commentEntityOptional = commentRepository.findById(request.getId());
        CommentEntity commentDetail = commentEntityOptional.orElseThrow(() -> new RuntimeException("Comment not found"));

        // 클라이언트의 IP 주소를 가져옵니다.
        String clientIpAddress = httpServletRequest.getHeader("X-Forwarded-For");
        if (clientIpAddress == null || clientIpAddress.isEmpty()) {
            // 프록시 서버를 통해 요청이 전달되지 않은 경우, 클라이언트의 진짜 IP 주소는 getRemoteAddr() 메소드로 가져올 수 있습니다.
            clientIpAddress = httpServletRequest.getRemoteAddr();
        }

        //업데이트 로직
        if (commentDetail.getClientIP().equals(clientIpAddress)) {
            commentDetail.setComments(request.getComment());
            commentRepository.save(commentDetail); // 변경된 내용 저장
        }

        return "redirect:/detail?id=" + request.getNewsId(); // detail.html 템플릿을 반환합니다.
    }

    // 댓글 삭제
    @GetMapping("/commentRemove")
    public String commentRemove(@RequestParam(value = "id") long id, HttpServletRequest httpServletRequest) {
        // 댓글 find
        Optional<CommentEntity> commentEntityOptional = commentRepository.findById(id);
        CommentEntity commentDetail = commentEntityOptional.orElseThrow(() -> new RuntimeException("Comment not found"));

        // 클라이언트의 IP 주소를 가져옵니다.
        String clientIpAddress = httpServletRequest.getHeader("X-Forwarded-For");
        if (clientIpAddress == null || clientIpAddress.isEmpty()) {
            // 프록시 서버를 통해 요청이 전달되지 않은 경우, 클라이언트의 진짜 IP 주소는 getRemoteAddr() 메소드로 가져올 수 있습니다.
            clientIpAddress = httpServletRequest.getRemoteAddr();
        }

        //삭제 로직
        if (commentDetail.getClientIP().equals(clientIpAddress)) {
            commentRepository.deleteById(id);
        }

        return "redirect:/detail?id=" + commentDetail.getNewsId(); // news.html 템플릿을 반환합니다.
    }
}
