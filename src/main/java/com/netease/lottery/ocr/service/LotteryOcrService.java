package com.netease.lottery.ocr.service;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageHelper;
import net.sourceforge.tess4j.util.LoadLibs;

@Service
public class LotteryOcrService
{
	private final Log log = LogFactory.getLog(getClass());

	@Autowired
	private Properties ocrProps;

	private Tesseract instanceSuperLotto;

	@PostConstruct
	public void init()
	{
		instanceSuperLotto = new Tesseract();
		instanceSuperLotto.setLanguage("mergedSuperLotto");
		instanceSuperLotto.setDatapath(LoadLibs.extractTessResources("tessdata").getAbsolutePath());
		for (Object key : ocrProps.keySet())
		{
			instanceSuperLotto.setTessVariable(key.toString(), ocrProps.getProperty(key.toString()));
		}
	}

	public Map<String, String> ocrSuperLotto(MultipartFile file)
			throws FileNotFoundException, IOException, TesseractException
	{
		BufferedImage image = ImageIO.read(file.getInputStream());
		// 图片锐化,自己使用中影响识别率的主要因素是针式打印机字迹不连贯,所以锐化反而降低识别率
		image = ImageHelper.convertImageToBinary(image);
		// 图片放大5倍,增强识别率(很多图片本身无法识别,放大1.5倍时就可以轻易识
		image = ImageHelper.getScaledInstance(image, (int) (image.getWidth() * 1.5), (int) (image.getHeight() * 1.5));

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
		for (int i = 0; i < lines.length;i++) {
			if (basePeriodLineNumber == -1 && lines[i].matches("[\\s\\S]*第[0-9 ]{1,}期[\\s\\S]*")) {
				basePeriodLineNumber = i;
			}
			if (baseAmountLineNumber == -1 && lines[i].matches("[\\s\\S]*(\\d)+[ ]*元[\\s\\S]*")) {
				baseAmountLineNumber = i;
			}
			if (isSingle) {
				if (baseCodeLineNumber == -1 && lines[i].contains("+") && lines[i].matches("[\\s\\S]*((\\d){2}|\\d \\d)[\\s\\S]*")) {
					 baseCodeLineNumber = i;
				}
			} else {
				if (baseCodeLineNumber == -1 && lines[i].contains("前区") && lines[i].matches("[\\s\\S]*((\\d){2}|\\d \\d)[\\s\\S]*")) {
					 baseCodeLineNumber = i;
				}
			}
		}

		if (lines.length >= basePeriodLineNumber + 1) {
			if (basePeriodLineNumber != -1) {
				if (basePeriodLineNumber >= 5) {
					try {
						resultMap.put("periodId",(Integer.parseInt(removeChineseChar(
								getFirstStringMatchReg(lines[basePeriodLineNumber], "第[0-9 ]{1,}期"), "第", "期"))+1)+""); // 暂时不考虑从第4行取投注时间来算期次
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
				else
					resultMap.put("periodId", removeChineseChar(
							getFirstStringMatchReg(lines[basePeriodLineNumber], "第[0-9 ]{1,}期"), "第", "期")); // 暂时不考虑从第4行取投注时间来算期次
			}
			if (lines.length >= baseAmountLineNumber + 1) {
				if (baseAmountLineNumber != -1) {
					String multiple = removeChineseChar(
							getFirstStringMatchReg(lines[baseAmountLineNumber], "(\\d){1}[ ]*倍"), "倍");
					if (multiple == null)
						multiple = "1"; //倍数默认为1
					resultMap.put("multiple", multiple);
					resultMap.put("added", getFirstStringMatchReg(lines[4], "追加"));
					resultMap.put("amount",
							removeChineseChar(getFirstStringMatchReg(lines[baseAmountLineNumber], "(\\d)+[ ]*元"), "元"));
				}
				if (baseCodeLineNumber != -1 && lines.length >= baseCodeLineNumber + 1) {
					if (isSingle) {
						String codes = "";
						for (int i = baseCodeLineNumber; i < lines.length; i++) {
							if (lines[i].contains("+") && getRegCount(lines[i], "((\\d){2}|\\d \\d(\\+| ))") >= 3) {
								String code = replaceSpaceInLine(
										lines[i].substring(getRegStart(lines[i], "((\\d){2}|\\d \\d(\\+| ))")));
								codes += code + ",";
							}
						}
						resultMap.put("codes", codes.substring(0, codes.length() - 1));
					} else {
						String frontCodes = "";
						String backCodes = "";
						for (int i = baseCodeLineNumber; i < lines.length; i++) {
							if (lines[i].contains("前区")) {
								int j = i;
								for (; j < lines.length; j++) {
									if (lines[j].contains("后区"))
										break;
									String code = replaceSpaceInLine(
											lines[j].substring(getRegStart(lines[j], "((\\d){2}|\\d \\d(\\+| ))")));
									frontCodes += code + " ";
								}
								i = j;
								String code = replaceSpaceInLine(
										lines[i].substring(getRegStart(lines[i], "((\\d){2}|\\d \\d(\\+| ))")));
								backCodes += code + " ";
							}
						}
						resultMap.put("frontCodes", frontCodes.substring(0, frontCodes.length() - 1));
						resultMap.put("backCodes", backCodes.substring(0, backCodes.length() - 1));
					}
				}
			}
		}
		return resultMap;
	}
	
	private String removeChineseChar(String content, String... removals){
		if (content == null)
			return null;
		for (String removal : removals) {
			content = content.replace(removal, "");
		}
		return content.trim();
	}

	private String getFirstStringMatchReg(String content, String reg)
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

	private int getRegCount(String content, String reg)
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

	private int getRegStart(String content, String reg)
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
			if(isSpaceShallBeReplaced(index, content)) {
				indexList.add(index);
			}
		}
		char[] contentArr = content.toCharArray();
		for (Integer index : indexList) {
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
}
