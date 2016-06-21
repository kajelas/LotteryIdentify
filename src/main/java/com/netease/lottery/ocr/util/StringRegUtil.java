package com.netease.lottery.ocr.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class StringRegUtil
{
	public String removeChineseChar(String content, String... removals)
	{
		if (content == null)
			return null;
		for (String removal : removals)
		{
			content = content.replace(removal, "");
		}
		return content.trim();
	}

	public String getFirstStringMatchReg(String content, String reg)
	{
		if (content == null)
			return null;
		Pattern p = Pattern.compile(reg);
		Matcher m = p.matcher(content);
		if (m.find())
			return m.group();
		else
			return null;
	}

	public int getRegCount(String content, String reg)
	{
		if (content == null)
			return 0;
		int count = 0;
		Pattern p = Pattern.compile(reg);
		Matcher m = p.matcher(content);
		while (m.find())
			count++;

		return count;
	}

	public int getRegStart(String content, String reg)
	{
		if (content == null)
			return -1;
		Pattern p = Pattern.compile(reg);
		Matcher m = p.matcher(content);
		if (m.find())
		{
			return m.start();
		}
		else
			return 0;
	}

	public String getStringWithoutLastChar(String input)
	{
		if (input == null || input.isEmpty())
		{
			return input;
		}
		return input.substring(0, input.length() - 1);
	}
}
