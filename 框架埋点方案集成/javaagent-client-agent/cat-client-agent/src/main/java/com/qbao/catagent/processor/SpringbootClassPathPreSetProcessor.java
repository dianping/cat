/**
 * 
 */
package com.qbao.catagent.processor;

/**
 * @author andersen
 *
 */
public class SpringbootClassPathPreSetProcessor extends AbstractClassPathPreSetProcessor {

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
		return false;
	}

}
