package com.dianping.cat.joblet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Partitioner;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JobletMeta {
	boolean combine() default false;

	String description();

	Class<? extends Writable> keyClass();

	String name();

	Class<? extends Partitioner<?, ?>> partitioner() default DefaultPartitioner.class;

	int reducerNum() default 3;

	Class<? extends Writable> valueClass();

	public class DefaultPartitioner extends Partitioner<Object, Object> {
		@Override
		public int getPartition(Object key, Object value, int numPartitions) {
			throw new UnsupportedOperationException("This should not be called!");
		}
	}
}
