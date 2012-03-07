package com.dianping.cat.job.sql;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class SqlStatementValue implements Writable {

	public int m_flag;

	public double m_value;
	
	public SqlStatementValue(){
		
	}

	public int getFlag() {
   	return m_flag;
   }

	public double getValue() {
   	return m_value;
   }

	public SqlStatementValue(int flag, double value) {
		m_flag = flag;
		m_value = value;
	}

	@Override
	public void readFields(DataInput input) throws IOException {
		m_flag = input.readInt();
		m_value = input.readDouble();
	}

	@Override
	public void write(DataOutput output) throws IOException {
		output.writeInt(m_flag);
		output.writeDouble(m_value);
	}

}
