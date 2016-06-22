package com.netease.lottery.ocr.service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.netease.lottery.ocr.util.StringRegUtil;

import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.util.ImageHelper;
import net.sourceforge.tess4j.util.LoadLibs;

@Service
public class SuperLottoOcrService implements LotteryOcrService
{
	private final Logger log = Logger.getLogger(getClass());

	@Autowired
	@Qualifier("tesseractProps")
	private Properties ocrProps;

	@Autowired
	private StringRegUtil stringRegUtil;

	private Tesseract1 instanceSuperLotto;

	@PostConstruct
	public void init()
	{
		//log.info(System.getProperty("java.io.tmpdir"));
		instanceSuperLotto = new Tesseract1();
		instanceSuperLotto.setLanguage("mergedSuperLotto");
		instanceSuperLotto.setDatapath(LoadLibs.extractTessResources("tessdata").getAbsolutePath());
		for (Object key : ocrProps.keySet())
		{
			instanceSuperLotto.setTessVariable(key.toString(), ocrProps.getProperty(key.toString()));
		}
	}

	@Override
	public Map<String, String> ocr(MultipartFile file) throws Exception
	{
		//log.info(System.getProperty("java.io.tmpdir"));
		log.info("uploaded...");
		BufferedImage image = ImageIO.read(file.getInputStream());
		// 图片锐化,自己使用中影响识别率的主要因素是针式打印机字迹不连贯,所以锐化反而降低识别率
		image = ImageHelper.convertImageToBinary(image);
		// 图片放大5倍,增强识别率(很多图片本身无法识别,放大1.5倍时就可以轻易识
		image = ImageHelper.getScaledInstance(image, (int) (image.getWidth() * 1.5), (int) (image.getHeight() * 1.5));

		//System.out.println(LoadLibsForWeb.TESS4J_TEMP_DIR);
		//instanceSuperLotto.setDatapath(LoadLibsForWeb.TESS4J_TEMP_DIR);
		log.info("pre-processed...");
		String result = instanceSuperLotto.doOCR(image);
		log.info(result);
		return mergeOcrResult(result);
	}

	private Map<String, String> mergeOcrResult(String ocrResult)
	{
		boolean isSingle = false;
		if (ocrResult.split("\\+").length >= 5)
			isSingle = true;

		Map<String, String> resultMap = new LinkedHashMap<String, String>();

		String[] lines = ocrResult.split("\n");
		int basePeriodLineNumber = -1;
		int baseAmountLineNumber = -1;
		int baseCodeLineNumber = -1;
		for (int i = 0; i < lines.length; i++)
		{
			if (basePeriodLineNumber == -1 && lines[i].matches("[\\s\\S]*第( )*1( )*[0-9 ]{4}期[\\s\\S]*"))
			{
				basePeriodLineNumber = i;
			}
			if (baseAmountLineNumber == -1 && lines[i].matches("[\\s\\S]*(\\d)+[ ]*元[\\s\\S]*"))
			{
				baseAmountLineNumber = i;
			}
			if (isSingle)
			{
				if (baseCodeLineNumber == -1 && lines[i].contains("+")
						&& lines[i].matches("[\\s\\S]*((\\d){2}|\\d \\d)[\\s\\S]*"))
				{
					baseCodeLineNumber = i;
				}
			}
			else
			{
				if (baseCodeLineNumber == -1 && lines[i].contains("前区")
						&& lines[i].matches("[\\s\\S]*((\\d){2}|\\d \\d)[\\s\\S]*"))
				{
					baseCodeLineNumber = i;
				}
			}
		}

		if (lines.length >= basePeriodLineNumber + 1)
		{
			if (basePeriodLineNumber != -1)
			{
				if (basePeriodLineNumber >= 5)
				{
					try
					{
						resultMap
								.put("periodId",
										(Integer.parseInt(
												stringRegUtil.removeChineseChar(
														stringRegUtil.getFirstStringMatchReg(
																lines[basePeriodLineNumber], "第( )*1( )*[0-9 ]{4}期"),
														"第", "期"))
												+ 1) + ""); // 暂时不考虑从第4行取投注时间来算期次
					}
					catch (NumberFormatException e)
					{
						resultMap.put("periodId", null); // 暂时不考虑从第4行取投注时间来算期次
					}
				}
				else
					resultMap.put("periodId", stringRegUtil.removeChineseChar(
							stringRegUtil.getFirstStringMatchReg(lines[basePeriodLineNumber], "第( )*1( )*[0-9 ]{4}期"),
							"第", "期")); // 暂时不考虑从第4行取投注时间来算期次
			}
			if (lines.length >= baseAmountLineNumber + 1)
			{
				if (baseAmountLineNumber != -1)
				{
					String multiple = stringRegUtil.removeChineseChar(
							stringRegUtil.getFirstStringMatchReg(lines[baseAmountLineNumber], "(\\d){1}[ ]*倍"), "倍");
					if (multiple == null)
						multiple = "1"; //倍数默认为1
					resultMap.put("multiple", multiple);
					resultMap.put("added",
							stringRegUtil.getFirstStringMatchReg(lines[baseAmountLineNumber], "追加") != null ? "1"
									: null);
					resultMap.put("amount", stringRegUtil.removeChineseChar(
							stringRegUtil.getFirstStringMatchReg(lines[baseAmountLineNumber], "(\\d)+[ ]*元"), "元"));
				}

				if (baseCodeLineNumber != -1 && lines.length >= baseCodeLineNumber + 1)
				{
					if (isSingle)
					{
						String codes = "";
						for (int i = baseCodeLineNumber; i < lines.length; i++)
						{
							if (lines[i].contains("+")
									&& stringRegUtil.getRegCount(lines[i], "((\\d){2}|\\d \\d(\\+| ))") >= 3)
							{
								String code = replaceSpaceInLine(lines[i]
										.substring(stringRegUtil.getRegStart(lines[i], "((\\d){2}|\\d \\d(\\+| ))")));
								codes += code + ",";
							}
						}
						resultMap.put("codes", stringRegUtil.getStringWithoutLastChar(codes));
					}
					else
					{
						List<String> keywordList = new ArrayList<String>();
						List<String> stopKeywordList = new ArrayList<String>();
						if (lines[baseCodeLineNumber].contains("前区胆")) //胆拖
						{
							keywordList.add("frontCodesDan");
							keywordList.add("frontCodesTuo");
							keywordList.add("backCodesDan");
							keywordList.add("backCodesTuo");
							stopKeywordList.add("前区");
							stopKeywordList.add("后区");
							stopKeywordList.add("后区");
							getCodeBeforeKeyword(lines, keywordList, stopKeywordList, baseCodeLineNumber, resultMap);
						}
						else if (lines[baseCodeLineNumber].contains("前区")) //复式
						{
							keywordList.add("frontCodes");
							keywordList.add("backCodes");
							stopKeywordList.add("后区");
							getCodeBeforeKeyword(lines, keywordList, stopKeywordList, baseCodeLineNumber, resultMap);
						}
					}
				}
			}
		}
		return resultMap;
	}

	private String replaceSpaceInLine(String content)
	{
		if (content == null)
			return null;
		Pattern p = Pattern.compile(" ");
		Matcher m = p.matcher(content);
		List<Integer> indexList = new ArrayList<Integer>();
		while (m.find())
		{
			int index = m.start();
			if (isSpaceShallBeReplaced(index, content))
			{
				indexList.add(index);
			}
		}
		char[] contentArr = content.toCharArray();
		for (Integer index : indexList)
		{
			contentArr[index] = 'm';
		}
		return new String(contentArr).replace("m", "");
	}

	private boolean isSpaceShallBeReplaced(int index, String content)
	{
		if (content == null)
			return false;
		if (index >= 2 && index + 2 < content.length())
		{
			if ((content.charAt(index - 1) >= '0' && content.charAt(index - 1) <= '9'
					&& content.charAt(index - 2) >= '0' && content.charAt(index - 2) <= '9')
					|| (content.charAt(index + 1) >= '0' && content.charAt(index + 1) <= '9'
							&& content.charAt(index + 2) >= '0' && content.charAt(index + 2) <= '9'))
			{
				return false;
			}
			else
				return true;
		}
		else
		{
			return true;
		}
	}

	private void getCodeBeforeKeyword(String[] lines, List<String> keywordList, List<String> stopKeywordList, int index,
			Map<String, String> resultMap)
	{
		for (int i = 0; i < keywordList.size(); i++)
		{
			String result = "";
			if (i >= stopKeywordList.size())
			{
				do
				{
					result += replaceSpaceInLine(lines[index]
							.substring(stringRegUtil.getRegStart(lines[index], "((\\d){2}|\\d \\d(\\+| ))"))) + " ";
					index++;
				}
				while (lines[index].matches("[\\d\\s]*") && index < lines.length);
			}
			else
			{
				do
				{
					result += replaceSpaceInLine(lines[index]
							.substring(stringRegUtil.getRegStart(lines[index], "((\\d){2}|\\d \\d(\\+| ))"))) + " ";
					index++;
				}
				while (!lines[index].contains(stopKeywordList.get(i)) && index < lines.length);
			}
			resultMap.put(keywordList.get(i), stringRegUtil.getStringWithoutLastChar(result));
		}
	}
}
