/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qbao.catagent.processor;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.qbao.catagent.ClassPathPreSetProcessor;

/**
 * 负责在进行类加载前加载指定
 * 
 * @author andersen
 *
 */
public abstract class AbstractClassPathPreSetProcessor implements ClassPathPreSetProcessor {
	// 保存已经加载过指定路径类的classloader
	private ConcurrentHashMap<ClassLoader, Method> invokedMethods = new ConcurrentHashMap<ClassLoader, Method>();
	// 配置文件中指定cat-client相关jar包位置的key
	private final String DEFAULT_CAT_CLIENT_PROPERTY_NAME = "cat.client.path";
	// 需加载的cat-client相关jar包
	private List<File> catClientJarFiles = new ArrayList<File>();
	// 配置文件中指定plugin相关jar包位置的key
	private final String DEFAULT_PLUGIN_PROPERTY_NAME = "cat.plugins.path";
	// 需加载的客户端plugin相关jar包
	private List<File> pluginJarFiles = new ArrayList<File>();
	// 默认方法
	private Method DEFAULT_METHOD = null;
	// 能否预设类路径
	private boolean enable = true;
	// 反射生成的classloader类名
	private final String deleLoader = "sun.reflect.DelegatingClassLoader";
	// 配置的忽略的classloader类名
	public List<String> loadersToSkip = null;
	// 跟aspectj保持一致，可配置跳过的classloader
	private final String LOADER_TO_SKIP_CLASSLOADER_PROPERTY_NAME = "aj.weaving.loadersToSkip";

	@Override
	public void initialize(Properties p) {
		if (p.getProperty(DEFAULT_CAT_CLIENT_PROPERTY_NAME) == null){
			System.out.println(String.format("Warn: CatAgent disabled , Missing %s in config file", DEFAULT_CAT_CLIENT_PROPERTY_NAME));
			enable = false;
			return;
		}
		if (p.getProperty(DEFAULT_PLUGIN_PROPERTY_NAME) == null){
			System.out.println(String.format("Warn: CatAgent disabled , Missing %s in config file", DEFAULT_PLUGIN_PROPERTY_NAME));
			enable = false;
			return;
		}
		
		// 组装忽略的classloader列表
		String loadersToSkipProperty = System.getProperty(LOADER_TO_SKIP_CLASSLOADER_PROPERTY_NAME, "");
		StringTokenizer st = new StringTokenizer(loadersToSkipProperty, ",");
		if (loadersToSkipProperty != null && loadersToSkip == null) {
			if (st.hasMoreTokens()) {
				loadersToSkip = new ArrayList<String>();
			}
			while (st.hasMoreTokens()) {
				String nextLoader = st.nextToken();
				loadersToSkip.add(nextLoader);
			}
		}

		// 初始化默认方法
		try {
			DEFAULT_METHOD = new Object().getClass().getMethod("toString");
		} catch (NoSuchMethodException e) {
			enable = false;
		} catch (SecurityException e) {
			enable = false;
		}

		// 初始化cat-client相关jar包路径
		File catClientPath = new File(p.getProperty(DEFAULT_CAT_CLIENT_PROPERTY_NAME));
		if (catClientPath.exists() == false) {
			System.out.println("Warn:  CatAgent disabled! file structure is not meet needs!");
			enable = false;
			return;
		}
		addJars(catClientPath, catClientJarFiles);

		// 初始化cat-plugins相关jar包路径
		File catPluginsPath = new File(p.getProperty(DEFAULT_PLUGIN_PROPERTY_NAME));
		if (catPluginsPath.exists() == false) {
			System.out.println("Warn:  CatAgent disabled! file structure is not meet needs!");
			enable = false;
			return;
		}
		addJars(catPluginsPath, pluginJarFiles);

		if (catClientJarFiles.isEmpty() && pluginJarFiles.isEmpty()) {
			enable = false;
			return;
		}

	}

	/**
	 * 把指定目录下所有jar包加入指定列表
	 * 
	 * @param rootPath
	 * @param files
	 */
	private void addJars(File f, List<File> fileList) {
		if (f.isFile() && f.getName().endsWith(".jar")) {
			fileList.add(f);
		}
		if (f.isDirectory()) {
			for (File tmpF : f.listFiles()) {
				addJars(tmpF, fileList);
			}
		}
	}

	/**
	 * 在脂定classloader内加入cp路径
	 * 
	 * @param classLoader
	 */
	private void loadPluginJars(ClassLoader classLoader) {
		Method method = null;
		if ((method = (Method) invokedMethods.get(classLoader)) == null) {
			try {
				if (classLoader != null) {
					if (classLoader instanceof URLClassLoader) {
						// javaagent的classloader一般是AppClassLoader
						ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
						
						boolean shouldLoadCatJars = shouldLoadCatJars(classLoader, systemClassLoader);
						boolean shouldLoadPluginsJars = shouldLoadPluginsJars(classLoader, systemClassLoader);
						
						if (shouldLoadCatJars || shouldLoadPluginsJars){
							try {
								method = classLoader.getClass().getDeclaredMethod("addURL", new Class[] { URL.class });
							} catch (Throwable t) {
								method = classLoader.getClass().getSuperclass().getDeclaredMethod("addURL",
										new Class[] { URL.class });
							}
							if (method == null) {
								invokedMethods.put(classLoader, DEFAULT_METHOD);
								return;
							}
							method.setAccessible(true);
						}
						if (shouldLoadCatJars){
							for (File f : catClientJarFiles)
								method.invoke(classLoader, new Object[] { f.toURI().toURL() });
						}
						if (shouldLoadPluginsJars){
							for (File f : pluginJarFiles)
								method.invoke(classLoader, new Object[] { f.toURI().toURL() });
						}
						invokedMethods.put(classLoader, method == null ? DEFAULT_METHOD : method);
					}else{
						invokedMethods.put(classLoader, DEFAULT_METHOD);
					}
				}
			} catch (Exception e) {
				System.err.println("init ClassLoader path occured error");
			}
		}
	}

	@Override
	public byte[] preProcess(String className, byte[] bytes, ClassLoader classLoader,
			ProtectionDomain protectionDomain) {
		if (classLoader == null || className == null || deleLoader.equals(classLoader.getClass().getName())) {
			// skip boot loader, null classes (hibernate), or those from a
			// reflection loader
			return bytes;
		}
		// 忽略过指定classloader
		/*if (loadersToSkip != null) {
			if (loadersToSkip.contains(classLoader.getClass().getName())) {
				return bytes;
			}
		}*/

		// 如果没出问题，则加入预加载类
		if (enable) {
			loadPluginJars(classLoader);
		}
		return bytes;
	}
	
	/**
	 * 是否需要设置cat相关jar包路径
	 * @param preProcessLoader 正在加载的classloader
	 * @param systemClassLoader 本agent运行时系统loader
	 * @return true:需要加载  false:不需要加载
	 */
	protected abstract boolean shouldLoadCatJars(ClassLoader preProcessLoader, ClassLoader systemClassLoader);
	
	/**
	 * 是否需要设置plugin相关jar包路径
	 * @param preProcessLoader 正在加载的classloader
	 * @param systemClassLoader 本agent运行时系统loader
	 * @return true:需要加载  false:不需要加载
	 */
	protected abstract boolean shouldLoadPluginsJars(ClassLoader preProcessLoader, ClassLoader systemClassLoader);

}
