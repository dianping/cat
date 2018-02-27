/**
 * 
 */
package com.qbao.cat.plugin.common;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.qbao.cat.plugin.DefaultPluginTemplate;

/**
 * @author andersen
 *
 */
@Aspect
public abstract class ThreadPluginTemplate extends DefaultPluginTemplate {

	@Override
	@Pointcut
	public void scope() {}

	
	@Override
	@Around(POINTCUT_NAME)
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		return super.doAround(pjp);
	}


	@Override
	protected Transaction beginLog(ProceedingJoinPoint pjp) {
		String className = pjp.getTarget().getClass().getName();		
		Transaction transaction = Cat.newTransaction("Thread",className);
		return transaction;
	}
	
	@Override
	protected void endLog(Transaction transaction, Object retVal, Object... params) {}

}
