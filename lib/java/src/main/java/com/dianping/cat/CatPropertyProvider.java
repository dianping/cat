package com.dianping.cat;

import java.util.ServiceLoader;

/**
 * 应用属性配置SPI
 *  此为配置的接口和扩展点，如具体应用要扩展，请实现此接口并在应用如下文件中指定实现类
 *  META-INF\services\com.dianping.cat.CatPropertyProvider
 * @author qxo
 *
 */
public interface CatPropertyProvider {
	
	public static final CatPropertyProvider INST = ServiceLoader.load(CatPropertyProvider.class).iterator().next();

	public String getProperty(String name, String defaultValue);
}
