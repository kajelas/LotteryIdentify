package com.netease.lottery.ocr;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages =
{ "com.netease.lottery.ocr" })
public class OcrAppConfig
{
	private final Log log = LogFactory.getLog(getClass());

	@Bean
	public Properties tesseractProps() throws IOException
	{
		Properties properties = new Properties();
		properties.load(
				new InputStreamReader(new ClassPathResource("mergedSuperLotto.properties").getInputStream(), "UTF-8"));
		for (Object key : properties.keySet())
		{
			log.info(key.toString() + " " + properties.getProperty(key.toString()));
		}
		return properties;
	}
}
