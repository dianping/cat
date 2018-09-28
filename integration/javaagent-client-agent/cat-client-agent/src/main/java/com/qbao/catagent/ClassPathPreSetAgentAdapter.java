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
package com.qbao.catagent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Properties;

import com.qbao.catagent.processor.JettyClassPathPreSetProcessor;
import com.qbao.catagent.processor.NullOperProcessor;
import com.qbao.catagent.processor.SpringbootClassPathPreSetProcessor;
import com.qbao.catagent.processor.TomcatClassPathPreSetProcessor;
import com.qbao.catagent.util.AppTypeEnum;
import com.qbao.catagent.util.PropertyUtil;

public class ClassPathPreSetAgentAdapter implements ClassFileTransformer{
	private static final String DEFAULT_APPTYPE_PROPERTY_NAME = "app.type";
	
	private static ClassPathPreSetProcessor s_preProcessor;
	
	public ClassPathPreSetAgentAdapter(String confFilePath){
		Properties p = null;
		if (confFilePath == null || confFilePath.trim() == "" || !confFilePath.endsWith(".properties") || (p = PropertyUtil.getProperties(confFilePath)) == null){
			System.out.println("Warn: CatAgent disabled! missing config file after javaagent or file format is invalid!");
			s_preProcessor = new NullOperProcessor();
		}else{
			if (p.get(DEFAULT_APPTYPE_PROPERTY_NAME) == null){
				System.out.println(String.format("Warn: CatAgent disabled! missing %s setting in %s! ", DEFAULT_APPTYPE_PROPERTY_NAME, confFilePath));
				s_preProcessor = new NullOperProcessor();
			}else{
				String appType = String.valueOf(p.get(DEFAULT_APPTYPE_PROPERTY_NAME));
				switch(AppTypeEnum.getAppType(appType)){
				case TOMCAT :
					s_preProcessor = new TomcatClassPathPreSetProcessor();
					break;
				case JETTY :
					s_preProcessor = new JettyClassPathPreSetProcessor();
					break;
				case SPRINGBOOT :
					s_preProcessor = new SpringbootClassPathPreSetProcessor();
					break;
				default :
					System.out.println(String.format("Warn: CatAgent disabled! app.type[%s] is not supported! ", DEFAULT_APPTYPE_PROPERTY_NAME, appType));
					s_preProcessor = new NullOperProcessor();
					break;
				}
			}
		}
		s_preProcessor.initialize(p);
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if (className.contains("com/dianping/cat/message/internal/MessageIdFactory")){
			System.out.println(className + "	>>>>>>>>>>>>	" + loader.getClass().getName() + "$" + Integer.toHexString(loader.hashCode()));
		}
		return s_preProcessor.preProcess(className, classfileBuffer, loader, protectionDomain);
	}
	
}
