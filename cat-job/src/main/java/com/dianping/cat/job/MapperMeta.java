package com.dianping.cat.job;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dianping.cat.hadoop.mapreduce.MessageTreeWritable;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MapperMeta {
	Class<?> keyIn() default Object.class;

	Class<?> keyOut();

	Class<?> valueIn() default MessageTreeWritable.class;
	
	Class<?> valueOut();
}
