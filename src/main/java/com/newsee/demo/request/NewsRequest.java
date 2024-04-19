package com.newsee.demo.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsRequest {
	private String title;
	private String contents;
	private Long id;
}
