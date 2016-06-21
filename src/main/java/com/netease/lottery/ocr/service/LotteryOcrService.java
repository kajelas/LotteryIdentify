package com.netease.lottery.ocr.service;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface LotteryOcrService
{
	public Map<String, String> ocr(MultipartFile file) throws Exception;
}
