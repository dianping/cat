package com.dianping.cat.system.page.login;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface LoginAction {
	String[] includes() default {};

	String[] excludes() default {};
}