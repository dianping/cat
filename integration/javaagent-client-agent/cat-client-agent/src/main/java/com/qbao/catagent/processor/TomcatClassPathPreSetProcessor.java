/**
 * 
 */
package com.qbao.catagent.processor;

/**
 * 针对tomcat应用进行的路径设置
 * @author andersen
 *
 */
public class TomcatClassPathPreSetProcessor extends AbstractClassPathPreSetProcessor {

	/* (non-Javadoc)
	 * @see com.qbao.catagent.processor.AbstractClassPreLoadProcessor#shouldLoadCatJars(java.lang.ClassLoader, java.lang.ClassLoader)
	 */
	@Override
	protected boolean shouldLoadCatJars(ClassLoader preProcessLoader, ClassLoader systemClassLoader) {
		if (preProcessLoader == systemClassLoader){
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.qbao.catagent.processor.AbstractClassPreLoadProcessor#shuoldLoadPluginsJars(java.lang.ClassLoader, java.lang.ClassLoader)
	 */
	@Override
	protected boolean shouldLoadPluginsJars(ClassLoader preProcessLoader, ClassLoader systemClassLoader) {
		if (preProcessLoader.getParent() == systemClassLoader){
			return true;
		}
		if (preProcessLoader.getClass().getName().endsWith("WebappClassLoader")){
			return true;
		}
		return false;
	}

}
