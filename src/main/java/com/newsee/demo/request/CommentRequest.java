package com.newsee.demo.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {
	private String Comment;
	private Long NewsId;
	private String clientIP;
}
