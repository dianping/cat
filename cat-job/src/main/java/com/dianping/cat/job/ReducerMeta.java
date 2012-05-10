package com.dianping.cat.job;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ReducerMeta {

	Class<?> keyIn();

	Class<?> keyOut();

	Class<?> valueIn();

	Class<?> valueOut();
}
