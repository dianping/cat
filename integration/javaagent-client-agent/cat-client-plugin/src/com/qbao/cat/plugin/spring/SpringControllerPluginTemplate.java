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
public abstract class SpringControllerPluginTemplate extends DefaultPluginTemplate {
	
	@Override
	@Around(POINTCUT_NAME)
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		return super.doAround(pjp);
	}

	/* (non-Javadoc)
	 * @see com.qbao.cat.plugin.PluginTemplate#scope()
	 */
	@Override
	@Pointcut
	public void scope() {
	}

	/* (non-Javadoc)
	 * @see com.qbao.cat.plugin.DefaultPluginTemplate#beginLog(org.aspectj.lang.ProceedingJoinPoint)
	 */
	@Override
	protected Transaction beginLog(ProceedingJoinPoint pjp) {
		StringBuilder type = new StringBuilder();
		String packageStr = pjp.getSignature().getDeclaringType().getPackage().getName();
		StringTokenizer st = new StringTokenizer(packageStr, ".");
		for(int i=0;i<2;i++){
			type.append(st.nextToken());
			type.append(".");
		}
		type.append("Controller");
		Transaction transaction = Cat.newTransaction(type.toString(),pjp.getSignature().toShortString());
		return transaction;
	}

	/* (non-Javadoc)
	 * @see com.qbao.cat.plugin.DefaultPluginTemplate#endLog(com.dianping.cat.message.Transaction, java.lang.Object, java.lang.Object[])
	 */
	@Override
	protected void endLog(Transaction transaction, Object retVal, Object... params) {
		// TODO Auto-generated method stub
	}

}
