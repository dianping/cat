package com.dianping.cat.job.sql;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

public class SqlStatementValue implements Writable {

	public int m_flag;

	public double m_value;
	
	public Text m_sampleUrl;
	
	public SqlStatementValue(){
		m_sampleUrl = new Text();
	}

	public int getFlag() {
   	return m_flag;
   }

	public double getValue() {
   	return m_value;
   }
	
	public String getSampleUrl(){
		return m_sampleUrl.toString();
	}

	public SqlStatementValue(int flag, double value ,String url) {
		m_flag = flag;
		m_value = value;
		m_sampleUrl=new Text(url);
	}

	@Override
	public void readFields(DataInput input) throws IOException {
		m_flag = input.readInt();
		m_value = input.readDouble();
		m_sampleUrl.readFields(input);
	}

	@Override
	public void write(DataOutput output) throws IOException {
		output.writeInt(m_flag);
		output.writeDouble(m_value);
		m_sampleUrl.write(output);
	}
}
