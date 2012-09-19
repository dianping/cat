package com.dianping.cat.notify;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dianping.cat.notify.util.StringUtil;

public class BaseTest {
	
	protected ApplicationContext context;
	
    public BaseTest() {
    	 setup();
	}

	private  void setup(){
 		final String allSpringConfig = System.getProperty("spring.context", "cat-notify.xml,");
 		String[] springConfigs = allSpringConfig.split(",");
 		context = doInitSpringContext(springConfigs);
     }
     
     public static ApplicationContext doInitSpringContext(String... configFile) {
 		List<String> list = new ArrayList<String>();
 		for (String config : configFile) {
 			if (StringUtil.isNotBlank(config)) {
 				list.add(config);
 			}
 		}
 		return new ClassPathXmlApplicationContext(list.toArray(new String[] {}));
 	} 
     
}
