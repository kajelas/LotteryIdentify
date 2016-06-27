package com.netease.lottery.ocr.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.netease.lottery.ocr.service.LotteryOcrService;
import com.netease.lottery.ocr.util.JsonUtil;

@Controller
public class LotteryOcrController
{
	@Autowired
	private LotteryOcrService lotteryOcrService;

	@Autowired
	private JsonUtil jsonUtil;

	@RequestMapping("/lotteryUpload")
	@ResponseBody
	public String uploadLottery(@RequestParam("loto") CommonsMultipartFile[] files) throws Exception
	{
		Map<String, String> resultMap = lotteryOcrService.ocr(files[0]);
		return jsonUtil.serialize(resultMap);
	}

	@RequestMapping("/lotteryIdentifyPage.html")
	public String uploadLottery(ModelMap modelMap) throws Exception
	{
		return "lotteryIdentify";
	}
}
