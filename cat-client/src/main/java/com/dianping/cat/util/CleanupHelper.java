package com.dianping.cat.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;

public final class CleanupHelper {

	private CleanupHelper() {
		super();
	}

	private static Method method4getCleaner;
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};
	@SuppressWarnings("rawtypes")
   private static final Class[] EMPTY_CLASS_ARRAY = new Class[] {};

	private static boolean initCleanupMethod;
	private static Method method4clean;
	
	protected static Method initMethod(MappedByteBuffer mbyteBuffer) {
		try {
			Method method4getCleaner = mbyteBuffer.getClass().getDeclaredMethod("cleaner",
					EMPTY_CLASS_ARRAY);
			if (!method4getCleaner.isAccessible()) {
				method4getCleaner.setAccessible(true);
			}
			
			return method4getCleaner;
		} catch (NoSuchMethodException ex) {
			ex.printStackTrace();
			return null;
		} catch (SecurityException ex) {
			ex.printStackTrace();
			return null;
		}finally {
			initCleanupMethod = true;
		}
	}
	
	public static void cleanup(MappedByteBuffer m_byteBuffer) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if( method4getCleaner == null && !initCleanupMethod) {
			//java.nio.DirectByteBuffer.clenar sun.misc.Cleaner
			method4getCleaner = initMethod(m_byteBuffer);
		}
		if( method4getCleaner != null ) {
			Object v = method4getCleaner.invoke(m_byteBuffer, EMPTY_OBJECT_ARRAY);
			if( v != null ) {
				if( method4clean == null ) {
					method4clean = v.getClass().getDeclaredMethod("clean", EMPTY_CLASS_ARRAY);
					if( !method4clean.isAccessible()) {
						method4clean.setAccessible(true);
					}
				}
				method4clean.invoke(v, EMPTY_OBJECT_ARRAY);
			}
		}
	}
}
