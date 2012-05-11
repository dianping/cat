package com.dianping.cat.job;

import java.io.IOException;
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

	Class<? extends Reducer<?, ?, ?, ?>> combiner() default NoCombiner.class;

	int reducerNum() default 3;

	public class DefaultPartitioner extends Partitioner<Object, Object> {
		@Override
		public int getPartition(Object key, Object value, int numPartitions) {
			throw new UnsupportedOperationException("This should not be called!");
		}
	}

	public class NoCombiner extends Reducer<Object, Object, Object, Object> {
		@Override
		protected void reduce(Object key, Iterable<Object> values, Context context) throws IOException,
		      InterruptedException {
			throw new UnsupportedOperationException("This should not be called!");
		}
	}
}
