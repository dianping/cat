/**
 * 
 */
package com.qbao.cat.plugin.web;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public abstract class TomcatUrlPluginTemplate extends BaseUrlPluginTemplate {

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

		
	public HttpServletRequest getHttpRequest(ProceedingJoinPoint pjp){
		HttpServletRequest request = (HttpServletRequest)pjp.getArgs()[0];
		return request;
	}
	
	public HttpServletResponse getHttpResponse(ProceedingJoinPoint pjp){
		HttpServletResponse response = (HttpServletResponse)pjp.getArgs()[1];
		return response;
	}
	
	@Override
	protected void endLog(Transaction transaction,Object retVal,Object...params) {
		HttpServletRequest request = (HttpServletRequest)params[0];
		HttpServletResponse response = (HttpServletResponse)params[1];
		logResponseInfo(response);
		Throwable cause = (Throwable)request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
		if(isNotNull(cause)){
			Cat.logError(cause);
		}
	}

}
