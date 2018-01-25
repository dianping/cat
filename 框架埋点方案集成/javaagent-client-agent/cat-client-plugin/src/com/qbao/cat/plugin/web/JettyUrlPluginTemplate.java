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
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.qbao.cat.plugin.CatPluginConstants;

/**
 * @author andersen
 *
 */
@Aspect
public abstract class JettyUrlPluginTemplate extends BaseUrlPluginTemplate {

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
		HttpServletRequest request = (HttpServletRequest)pjp.getArgs()[2];
		return request;
	}
	
	public HttpServletResponse getHttpResponse(ProceedingJoinPoint pjp){
		HttpServletResponse response = (HttpServletResponse)pjp.getArgs()[3];
		return response;
	}
	
	@Override
	protected void endLog(Transaction transaction,Object retVal,Object...params) {
		HttpServletRequest request = (HttpServletRequest)params[2];
		HttpServletResponse response = (HttpServletResponse)params[3];
		logResponseInfo(response);
		Throwable cause = (Throwable)request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
		if(isNotNull(cause)){
			Cat.logError(cause);
		}
	}

}
