package com.newsee.demo.response;

import java.util.List;

import com.newsee.demo.entity.CommentEntity;
import com.newsee.demo.entity.NewsEntity;

import lombok.Data;

@Data
public class NewsDetailResponse {
	private NewsEntity news;
	private List<CommentEntity> comments;
}
