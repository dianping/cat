/**
 * 
 */
package com.qbao.catagent.processor;

/**
 * 针对jetty应用进行的路径设置
 * @author andersen
 *
 */
public class JettyClassPathPreSetProcessor extends AbstractClassPathPreSetProcessor {

	/* (non-Javadoc)
	 * @see com.qbao.catagent.processor.AbstractClassPathPreSetProcessor#shouldLoadCatJars(java.lang.ClassLoader, java.lang.ClassLoader)
	 */
	@Override
	protected boolean shouldLoadCatJars(ClassLoader preProcessLoader, ClassLoader systemClassLoader) {
		if (preProcessLoader == systemClassLoader){
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.qbao.catagent.processor.AbstractClassPathPreSetProcessor#shuoldLoadPluginsJars(java.lang.ClassLoader, java.lang.ClassLoader)
	 */
	@Override
	protected boolean shouldLoadPluginsJars(ClassLoader preProcessLoader, ClassLoader systemClassLoader) {
		if (preProcessLoader == systemClassLoader){
			return true;
		}
		if (preProcessLoader.getClass().getName().endsWith("WebAppClassLoader")){
			return true;
		}
		return false;
	}

}
