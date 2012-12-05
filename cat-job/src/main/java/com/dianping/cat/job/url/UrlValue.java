package com.dianping.cat.job.url;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

public class UrlValue implements Writable {

	public Text m_date;

	public double m_value;

	public UrlValue() {
		m_date = new Text();
	}

	public double getValue() {
		return m_value;
	}

	public UrlValue setValue(double value) {
		m_value = value;
		return this;
	}

	public Text getDate() {
		return m_date;
	}

	public UrlValue setDate(Text date) {
		m_date = date;
		return this;
	}

	@Override
	public void readFields(DataInput input) throws IOException {
		m_value = input.readDouble();
		m_date.readFields(input);
	}

	@Override
	public void write(DataOutput output) throws IOException {
		output.writeDouble(m_value);
		m_date.write(output);
	}

	public String toString() {
		return String.valueOf(m_value) + "\t";
	}
}
