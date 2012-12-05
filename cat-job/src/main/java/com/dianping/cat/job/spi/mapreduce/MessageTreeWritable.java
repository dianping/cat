package com.dianping.cat.job.spi.mapreduce;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class MessageTreeWritable implements Writable {
	private MessageTree m_tree = new DefaultMessageTree();

	private boolean m_completed;

	public MessageTreeWritable() {
	}

	// for testing purpose
	public MessageTreeWritable(MessageTree tree) {
		m_tree = tree;
	}

	public void complete() {
		m_completed = true;
	}

	public MessageTree get() {
		return m_tree;
	}

	public boolean isCompleted() {
		return m_completed;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		throw new UnsupportedOperationException(
		      "This method should never be called, please check with the author if any problem.");
	}

	@Override
	public void write(DataOutput out) throws IOException {
		throw new UnsupportedOperationException(
		      "This method should never be called, please check with the author if any problem.");
	}
}
