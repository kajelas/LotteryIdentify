package com.netease.lottery.ocr;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages =
{ "com.netease.lottery.ocr" })
public class OcrAppConfig
{
	//private final Log log = LogFactory.getLog(getClass());

	@Bean(name = "tesseractProps")
	public Properties tesseractProps() throws IOException
	{
		Properties properties = new Properties();
		properties.load(
				new InputStreamReader(new ClassPathResource("mergedSuperLotto.properties").getInputStream(), "UTF-8"));
		//		for (Object key : properties.keySet())
		//		{
		//			log.info(key.toString() + " " + properties.getProperty(key.toString()));
		//		}
		return properties;
	}

	@Bean
	public MultipartResolver multipartResolver()
	{
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		resolver.setDefaultEncoding("UTF-8");
		resolver.setMaxUploadSize(10485760000L);
		resolver.setMaxUploadSizePerFile(10485760000L);
		resolver.setMaxInMemorySize(Integer.MAX_VALUE);
		return resolver;
	}

	@Bean
	public ViewResolver viewResolver()
	{
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setContentType("text/html;charset=UTF-8");
		resolver.setSuffix(".jsp");
		return resolver;
	}
}
