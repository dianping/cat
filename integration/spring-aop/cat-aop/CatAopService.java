package com.dianping.cat.aop;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

@Aspect
public class CatAopService {

	@Around(value = "@annotation(CatAnnotation)")
	public Object aroundMethod(ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature joinPointObject = (MethodSignature) pjp.getSignature();
		Method method = joinPointObject.getMethod();

		Transaction t = Cat.newTransaction("method", method.getName());

		try {
			Object res = pjp.proceed();
			t.setSuccessStatus();
			return res;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.logError(e);
			throw e;
		} finally {
			t.complete();
		}

	}

}
