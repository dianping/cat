/**
 * 
 */
package com.qbao.cat.plugin.remote.dubbo;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.dianping.cat.message.Transaction;
import com.qbao.cat.plugin.CatPluginConstants;
import com.qbao.cat.plugin.remote.ClientPluginTemplate;

/**
 * @author andersen
 *
 */
@Aspect
public abstract class DubboClientPluginTemplate extends ClientPluginTemplate<RpcInvocation> {
	
	private static final ThreadLocal<AtomicBoolean> Entered = new ThreadLocal<AtomicBoolean>(){
		@Override
		protected AtomicBoolean initialValue() {
			return new AtomicBoolean(false);
		}
	};
	
	@Override
	@Pointcut
	public void scope() {
		
	}
	
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
	protected RpcInvocation getRequestContext(ProceedingJoinPoint pjp) {
		//≥ı ºªØinvoker
		RpcInvocation rpcInvocation =  (RpcInvocation)pjp.getArgs()[0];
		if(isNull(rpcInvocation.getInvoker())){
			rpcInvocation.setInvoker((Invoker<?>)pjp.getThis());
		}
		return (RpcInvocation)pjp.getArgs()[0];
	}

	@Override
	protected String getTransactionType() {
		return CatPluginConstants.TYPE_DUBBO_CLIENT;
	}

	@Override
	protected String getTransactionName(RpcInvocation requestContext) {
		return requestContext.getInvoker().getInterface().getSimpleName()+"."+requestContext.getMethodName();
	}

	@Override
	protected void sendClientAddr(RpcInvocation request, String key, String value) {
		request.setAttachment(key, value);
	}

	@Override
	protected void sendClientDomain(RpcInvocation request, String key, String value) {
		request.setAttachment(key, value);
	}

	@Override
	protected void specialHandling(RpcInvocation request) {
		
	}

	@Override
	protected void logTrace(RpcInvocation handler, Entry<String, String> entry) {
		handler.setAttachment(entry.getKey(),entry.getValue());
	}

	@Override
	protected boolean enableTrace(RpcInvocation handler) {
		handler.setAttachment(CatPluginConstants.D_CALL_TRACE_MODE, "trace");
		return true;
	}

	@Override
	protected void endLog(Transaction transaction, Object retVal, Object... params) {
		Result result = ((Result)retVal);
		String serverDomain = result.getAttachment(CatPluginConstants.D_CALL_SERVER_DOMAIN);
		String serverAddr = result.getAttachment(CatPluginConstants.D_CALL_SERVER_ADDR);
		if(isNotNull(serverDomain)){
			logServerDomain(serverDomain);
		}
		if(isNotNull(serverAddr)){
			logServerAddr(serverAddr);
		}
	}
}
