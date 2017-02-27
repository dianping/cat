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

	@Around(value = "execution(* *.*(..))")
	public void aroundMethod(ProceedingJoinPoint pjp) {
		MethodSignature joinPointObject = (MethodSignature) pjp.getSignature();
		Method method = joinPointObject.getMethod();
		boolean flag = method.isAnnotationPresent(CatAnnotation.class);

		if (flag) {
			Transaction t = Cat.newTransaction("method", method.getName());

			try {
				pjp.proceed();

				t.setSuccessStatus();
				t.complete();
			} catch (Throwable e) {
				t.setStatus(e);
				Cat.logError(e);
			} finally {
				t.complete();
			}
		} else {
			try {
				pjp.proceed();
			} catch (Throwable e) {
			}
		}
	}

}
