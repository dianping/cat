/**
 * 
 */
package com.qbao.catagent.processor;

import java.security.ProtectionDomain;
import java.util.Properties;

import com.qbao.catagent.ClassPathPreSetProcessor;

/**
 * 不做任何处理，直接返回输入的字节流
 * @author andersen
 *
 */
public class NullOperProcessor implements ClassPathPreSetProcessor {

	/* (non-Javadoc)
	 * @see com.qbao.catagent.ClassPreLoadProcessor#initialize(java.util.Properties)
	 */
	@Override
	public void initialize(Properties prop) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.qbao.catagent.ClassPreLoadProcessor#preProcess(java.lang.String, byte[], java.lang.ClassLoader, java.security.ProtectionDomain)
	 */
	@Override
	public byte[] preProcess(String className, byte[] bytes, ClassLoader classLoader,
			ProtectionDomain protectionDomain) {		
		return bytes;
	}

}
