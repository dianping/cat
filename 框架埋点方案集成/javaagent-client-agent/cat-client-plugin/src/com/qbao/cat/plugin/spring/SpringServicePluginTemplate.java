/**
 * 
 */
package com.qbao.cat.plugin.spring;

import java.util.StringTokenizer;

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
public abstract class SpringServicePluginTemplate extends DefaultPluginTemplate{
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
		StringBuilder type = new StringBuilder();
		String packageStr = pjp.getSignature().getDeclaringType().getPackage().getName();
		StringTokenizer st = new StringTokenizer(packageStr, ".");
		for(int i=0;i<2;i++){
			type.append(st.nextToken());
			type.append(".");
		}
		type.append("Service");
		Transaction transaction = Cat.newTransaction(type.toString(),pjp.getSignature().toShortString());
		return transaction;
	}
	
	@Override
	protected void endLog(Transaction transaction, Object retVal, Object... params) {}
}
