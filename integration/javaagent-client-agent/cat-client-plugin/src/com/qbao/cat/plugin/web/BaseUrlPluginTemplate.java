/**
 * 
 */
package com.qbao.cat.plugin.web;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.qbao.cat.plugin.CatPluginConstants;
import com.qbao.cat.plugin.DefaultPluginTemplate;

/**
 * @author andersen
 *
 */
public abstract class BaseUrlPluginTemplate extends DefaultPluginTemplate{

	/* (non-Javadoc)
	 * @see com.qbao.cat.plugin.DefaultPluginTemplate#beginLog(org.aspectj.lang.ProceedingJoinPoint)
	 */
	@Override
	protected Transaction beginLog(ProceedingJoinPoint pjp) {
		HttpServletRequest request = getHttpRequest(pjp);
		Transaction transaction = newTransaction(CatConstants.TYPE_URL,getConcreteUri(request.getRequestURI()));
		if(isNotNull(transaction)){
			logRequestClientInfo(request);
		}
		return transaction;
	}
	
	protected abstract HttpServletRequest getHttpRequest(ProceedingJoinPoint pjp);
	
	protected abstract HttpServletResponse getHttpResponse(ProceedingJoinPoint pjp);
	
	protected void logResponseInfo(HttpServletResponse response){
		Cat.logEvent(CatConstants.TYPE_URL, CatPluginConstants.TYPE_URL_SERVER_RESOPONSE_CODE, Message.SUCCESS, String.valueOf(response.getStatus()));
	}
	protected void logRequestClientInfo(HttpServletRequest req) {
		Cat.logEvent(CatConstants.TYPE_URL, CatPluginConstants.TYPE_URL_SERVER, Message.SUCCESS, getRequestServerInfo(req));
		String referer = req.getHeader("referer");
		if(isNotNull(referer)){
			Cat.logEvent(CatConstants.TYPE_URL, CatPluginConstants.TYPE_URL_SERVER_REFERER, Message.SUCCESS,referer);
		}
		String userAgent = req.getHeader("user-agent");
		if(isNotNull(userAgent)){
			Cat.logEvent(CatConstants.TYPE_URL, CatPluginConstants.TYPE_URL_SERVER_AGENT, Message.SUCCESS,userAgent);
		}
		Cat.logEvent(CatConstants.TYPE_URL,CatPluginConstants.TYPE_URL_METHOD, Message.SUCCESS, getRequestMethodInfo(req));
	}
	
	
	
	protected String getRequestMethodInfo(HttpServletRequest req){
		StringBuilder sb = new StringBuilder(256);
		sb.append(req.getScheme().toUpperCase()).append('/');
		sb.append(req.getMethod()).append(' ').append(req.getRequestURI());
		String qs = req.getQueryString();
		if (qs != null) {
			sb.append('?').append(qs);
		}
		return sb.toString();
	}
	
	protected String getRequestServerInfo(HttpServletRequest req){
		StringBuilder sb = new StringBuilder(1024);
		String ip = "";
		String ipForwarded = req.getHeader("x-forwarded-for");
		if (ipForwarded == null) {
			ip = req.getRemoteAddr();
		} else {
			ip = ipForwarded;
		}
		sb.append("IPS=").append(ip);
		sb.append("&VirtualIP=").append(req.getRemoteAddr());
		sb.append("&Server=").append(req.getServerName());
		return sb.toString();
	}
}
