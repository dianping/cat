/**
 * 
 */
package com.qbao.cat.plugin;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

/**
 * @author andersen
 *
 */
@Aspect
public abstract class TestPluginTemplate implements PluginTemplate {

	/* (non-Javadoc)
	 * @see cat.qbao.cat.plugin.PluginTemplate#scope()
	 */
	@Override
	@Pointcut
	public void scope() {
		// TODO Auto-generated method stub

	}
	
	@Around(POINTCUT_NAME)
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		System.out.println("Method Signatrue --- " + pjp.getSignature().toLongString());
		Object retVal = pjp.proceed();
		System.out.println("retrun value --- " + retVal);
		return retVal;
	}

	

}
