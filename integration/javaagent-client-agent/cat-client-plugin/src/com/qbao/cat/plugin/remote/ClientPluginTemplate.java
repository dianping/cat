/**
 * 
 */
package com.qbao.cat.plugin.remote;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.qbao.cat.plugin.constants.MyCatConstants;
import org.aspectj.lang.ProceedingJoinPoint;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Transaction;
import com.qbao.cat.plugin.CatPluginConstants;
import com.qbao.cat.plugin.DefaultPluginTemplate;

/**
 * @author andersen
 *
 */
public abstract class ClientPluginTemplate<T> extends DefaultPluginTemplate {
	

	protected RemoteContext getClientTrace() {
		RemoteContext context = new RemoteContext();
		Cat.logRemoteCallClient(context);
		return context;
	}
	@Override
	protected Transaction beginLog(ProceedingJoinPoint pjp) {
		Transaction transaction = null;
		if (isNotNull(pjp.getArgs()) && pjp.getArgs().length >= 1) {
			T requestContext = getRequestContext(pjp);
			transaction = newTransaction(getTransactionType(),getTransactionName(requestContext));
			//send client addr
			sendClientAddr(requestContext,getClientAddrDataKey(),getClientAddrData());
			//send client domain
			sendClientDomain(requestContext,getClientDomainDataKey(),getClientDomainData());
			
			specialHandling(requestContext);
			//记录本地Message传输到服务器
			logRemoteTrace(requestContext);
		}
		return transaction;
	}
	
	protected abstract T getRequestContext(ProceedingJoinPoint pjp);
	
	protected abstract String getTransactionType();
	
	protected abstract String getTransactionName(T requestContext);
	
	protected abstract void sendClientAddr(T request,String key,String value);
	
	protected abstract void sendClientDomain(T request,String key,String value);
	
	protected abstract void specialHandling(T request);
	
	protected abstract void logTrace(T handler, Entry<String, String> entry);
	
	protected abstract boolean enableTrace(T handler);
	
	/**
	 * 
	 * @return
	 */
	protected String getClientAddrDataKey(){
		return CatPluginConstants.D_CLIENT_ADDR;
	}
	
	protected String getServerAddrDataKey(){
		return CatPluginConstants.D_CALL_SERVER_ADDR;
	}
	
	protected String getServerDomainDataKey(){
		return CatPluginConstants.D_CALL_SERVER_DOMAIN;
	}
	
	protected String getClientAddrData(){
		return Cat.getManager().getThreadLocalMessageTree().getIpAddress();
	}
	
	protected String getClientDomainDataKey(){
		return CatPluginConstants.D_CLIENT_DOMAIN;
	}
	
	
	protected String getClientDomainData(){
		return Cat.getManager().getThreadLocalMessageTree().getDomain();
	}
	
	protected String getDomainKey(){
		return MyCatConstants.E_CALL_APP;
	}
	
	protected void logServerDomain(String serverDomain){
		Cat.logEvent(MyCatConstants.E_CALLEE_APP,serverDomain);
	}
	
	protected void logServerAddr(String serverAddr){
		Cat.logEvent(MyCatConstants.E_CALLEE_ADDR,serverAddr);
	}
	
	protected void logRemoteTrace(T handler) {
		if (enableTrace(handler)) {
			for (Entry<String, String> entry : this.getClientTrace().getAllData().entrySet()) {
				logTrace(handler, entry);
			}
		}
	}
	
	public static class RemoteContext implements Cat.Context {

		private Map<String, String> traceData = new HashMap<String, String>();

		@Override
		public void addProperty(String key, String value) {
			traceData.put(key, value);
		}

		@Override
		public String getProperty(String key) {
			return traceData.get(key);
		}

		public Map<String, String> getAllData() {
			return traceData;
		}

	}

}
