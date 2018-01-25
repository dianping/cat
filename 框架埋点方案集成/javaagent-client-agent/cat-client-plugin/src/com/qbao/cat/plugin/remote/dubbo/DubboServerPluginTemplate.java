/**
 * 
 */
package com.qbao.cat.plugin.remote.dubbo;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
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
public abstract class DubboServerPluginTemplate extends ServerPluginTemplate<Invocation>{
	
	@Override
	@Around(POINTCUT_NAME)
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		return super.doAround(pjp);
	}
	@Override
	@Pointcut
	public void scope() {
		
	}
	@Override
	protected Transaction beginLog(ProceedingJoinPoint pjp) {
		Transaction transaction = null;
		RpcInvocation request = (RpcInvocation)pjp.getArgs()[0];
		if(this.isEnableTrace(request)){
			logClientTrace(request);
			transaction = newTransaction(CatPluginConstants.TYPE_DUBBO_SERVER,getRpcName(request,(Invoker<?>)pjp.getTarget()));
			logClientInfo(request.getAttachment(CatPluginConstants.D_CLIENT_ADDR),request.getAttachment(CatPluginConstants.D_CLIENT_DOMAIN));
		}
		return transaction;
	}
	
	private String getRpcName(RpcInvocation request,Invoker<?> invoker){
		return invoker.getInterface().getName()+"."+request.getMethodName();
	}
	
	@Override
	public boolean isEnableTrace(Invocation request) {
		return request.getAttachment(CatPluginConstants.D_CALL_TRACE_MODE) != null;
	}
	
	protected void logClientTrace(Invocation request){
		logClientTrace(request.getAttachment(Cat.Context.CHILD),request.getAttachment(Cat.Context.PARENT),request.getAttachment(Cat.Context.ROOT));
	}

	@Override
	protected void endLog(Transaction transaction, Object retVal, Object... params) {
		((RpcResult)retVal).setAttachment(CatPluginConstants.D_CALL_SERVER_DOMAIN, Cat.getManager().getDomain());
		((RpcResult)retVal).setAttachment(CatPluginConstants.D_CALL_SERVER_ADDR, Cat.getManager().getThreadLocalMessageTree().getIpAddress());
	}
}
