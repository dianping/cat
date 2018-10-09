/**
 * 
 */
package com.qbao.cat.plugin.remote.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Transaction;
import com.qbao.cat.plugin.CatPluginConstants;
import com.qbao.cat.plugin.remote.ServerPluginTemplate;

/**
 * @author andersen
 *
 */
@Aspect
public abstract class HttpServerPluginTemplate extends ServerPluginTemplate<HttpServletRequest> {
	@Override
	@Around(POINTCUT_NAME)
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		return super.doAround(pjp);
	}

	@Override
	@Pointcut()
	public void scope() {
		
	}
	@Override
	protected Transaction beginLog(ProceedingJoinPoint pjp) {
		Transaction transaction = null;
		HttpServletRequest request = (HttpServletRequest)pjp.getArgs()[0];
		if(this.isEnableTrace(request)){
			logClientTrace(request);
			transaction = newTransaction(CatPluginConstants.TYPE_HTTP_SERVER,getConcreteUri(request.getRequestURI()));
			HttpServletResponse response = (HttpServletResponse)pjp.getArgs()[1];
			logClientInfo(request.getHeader(CatPluginConstants.D_CLIENT_ADDR),request.getHeader(CatPluginConstants.D_CLIENT_DOMAIN));
			notifyServerInfo(response);
		}
		return transaction;
	}
	
	protected void notifyServerInfo(HttpServletResponse response){
		response.setHeader(CatPluginConstants.D_CALL_SERVER_DOMAIN, Cat.getManager().getDomain());
		response.setHeader(CatPluginConstants.D_CALL_SERVER_ADDR, Cat.getManager().getThreadLocalMessageTree().getIpAddress());
	}

	@Override
	public boolean isEnableTrace(HttpServletRequest request) {
		return request.getHeader(CatPluginConstants.D_CALL_TRACE_MODE) != null;
	}
	
	protected void logClientTrace(HttpServletRequest request){
		logClientTrace(request.getHeader(Cat.Context.CHILD),request.getHeader(Cat.Context.PARENT),request.getHeader(Cat.Context.ROOT));
	}

	@Override
	protected void endLog(Transaction transaction, Object retVal, Object... params) {
		
	}
}
