/**
 * 
 */
package com.qbao.cat.plugin.remote.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Transaction;
import com.qbao.cat.plugin.CatPluginConstants;
import com.qbao.cat.plugin.remote.ClientPluginTemplate;

/**
 * @author andersen
 *
 */
@Aspect
public abstract class HttpClientPluginTemplate extends ClientPluginTemplate<HttpMessage> {
	private static final ThreadLocal<AtomicBoolean> Entered = new ThreadLocal<AtomicBoolean>(){
		@Override
		protected AtomicBoolean initialValue() {
			return new AtomicBoolean(false);
		}
	};
	@Override
	@Pointcut
	public void scope() {}
	
	@Override
	@Around(POINTCUT_NAME)
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		Object obj = null;
		boolean result = true;
		try{
			result = Entered.get().compareAndSet(false, true);
		}catch(Throwable t){}
		if(result){
			obj = super.doAround(pjp);
			try{
				Entered.get().set(false);
			}catch(Throwable tt){}
		}else{
			obj = pjp.proceed();
		}
		return obj;
		
	}

	@Override
	public Transaction beginLog(ProceedingJoinPoint pjp) {
		HttpHost httpHost = null;
		HttpUriRequest uriRequest = null;
		HttpRequest request = null;
		for(Object param : pjp.getArgs()){
			if(param instanceof HttpHost){
				httpHost = (HttpHost)param;
			}
			if(param instanceof HttpUriRequest){
				uriRequest = (HttpUriRequest)param;
			}
			if(param instanceof HttpRequest){
				request = (HttpRequest)param;
			}
		}
		if(isNotNull(uriRequest)){
			return logTransaction(uriRequest,uriRequest.getURI(),uriRequest.getMethod());
		}
		//if httpHost != null ,request must not null
		if(isNotNull(httpHost) && isNotNull(request)){
			try {
				return logTransaction(request,new URI(request.getRequestLine().getUri()),request.getRequestLine().getMethod());
			} catch (URISyntaxException e) {
			}
		}
		return null;
	}

	private Transaction logTransaction(HttpMessage message, URI uri,String method) {
		Transaction transaction;
		transaction = this.newTransaction(this.getTransactionType(),uri.getScheme()+"://"+uri.getAuthority()+getConcreteUri(uri.getPath()));
		sendClientAddr(message, getClientAddrDataKey(),getClientAddrData());
		sendClientDomain(message,getClientDomainDataKey(),getClientDomainData());
		Cat.logEvent("Http.Method", method);
		logRemoteTrace(message);
		return transaction;
	}

	@Override
	public boolean enableTrace(HttpMessage handler) {
		handler.setHeader(CatPluginConstants.D_CALL_TRACE_MODE, "trace");
		return true;
	}

	@Override
	public void endLog(Transaction transaction, Object retVal, Object... params) {
		Header addr = ((HttpResponse)retVal).getFirstHeader(getServerAddrDataKey());
		Header domain = ((HttpResponse)retVal).getFirstHeader(getServerDomainDataKey());
		if(isNotNull(addr)){
			logServerAddr(addr.getValue());
		}
		if(isNotNull(domain)){
			logServerDomain(domain.getValue());
		}
	}

	@Override
	public void logTrace(HttpMessage handler, Entry<String, String> entry) {
		handler.setHeader(entry.getKey(), entry.getValue());
	}

	@Override
	protected HttpMessage getRequestContext(ProceedingJoinPoint pjp) {
		return null;
	}

	@Override
	protected String getTransactionType() {
		return CatPluginConstants.TYPE_HTTP_CLIENT;
	}

	@Override
	protected String getTransactionName(HttpMessage requestContext) {
		return null;
	}

	@Override
	protected void sendClientAddr(HttpMessage request, String key, String value) {
		request.setHeader(key, value);
		
	}

	@Override
	protected void sendClientDomain(HttpMessage request, String key, String value) {
		request.setHeader(key, value);
	}
	
	@Override
	protected void specialHandling(HttpMessage request) {
		
	}
}
