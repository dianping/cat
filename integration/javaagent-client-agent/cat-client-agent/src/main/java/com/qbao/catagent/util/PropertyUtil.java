package com.qbao.catagent.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropertyUtil {
	public static Properties getProperties(String propertyFilePath) {
		Properties prop = new Properties();
		try {
			// 读取属性文件a.properties
			InputStream in = new BufferedInputStream(new FileInputStream(propertyFilePath));
			prop.load(new InputStreamReader(in, "utf-8")); /// 加载属性列表
			in.close();
			return prop;
		} catch (Exception e) {
			System.out.println("Warn: CatAgent can't resolve properties file : " + propertyFilePath);
			return null;
		}
	}
}
