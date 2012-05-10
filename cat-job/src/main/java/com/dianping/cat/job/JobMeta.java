package com.dianping.cat.job;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JobMeta {
	Class<? extends Mapper<?, ?, ?, ?>> mapper();

	String name();

	Class<? extends Partitioner<?, ?>> partitioner() default DefaultPartitioner.class;

	Class<? extends Reducer<?, ?, ?, ?>> reducer();

	int reducerNum() default 3;

	public class DefaultPartitioner extends Partitioner<Object, Object> {
		@Override
		public int getPartition(Object key, Object value, int numPartitions) {
			return (key.hashCode() & Integer.MAX_VALUE) % numPartitions;
		}
	}
}
