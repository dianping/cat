package com.dianping.cat.job.sql;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class SqlJobResult implements Writable {

	private static final double LONG_TIME = 50;

	private int m_count;

	private double m_sum;

	private double m_sum2;
	
	private int m_successCount;
	
	private int m_failureCount;

	private int m_longTimeCount;

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Count: ").append(m_count).append("\t").append("Sum: ").append(m_sum).append("\t")//
		      .append("Sum2: ").append(m_sum2).append("\t").append("Std: ").append(getStd());
		sb.append("\t").append("Success: ").append(m_successCount);
		sb.append("\t").append("Failure: ").append(m_failureCount).append("\t").append("Long: ").append(m_longTimeCount);
		return sb.toString();
	}

	public void add(double value, int flag) {
		m_count++;
		m_sum += value;
		m_sum2 = m_sum2 + value * value;
		if (flag == 1) {
			m_failureCount++;
		}else{
			m_successCount++;
		}
		if(value>LONG_TIME){
			m_longTimeCount++;
		}
	}

	public double getAvg() {
		if (m_count == 0) {
			return 0;
		}
		return m_sum / m_count;
	}

	private double getStd() {
		double ave = getAvg();
		return Math.sqrt(m_sum2 / m_count - 2 * ave * ave + ave * ave);
	}

	@Override
	public void readFields(DataInput arg0) throws IOException {
		throw new UnsupportedOperationException(
		      "This method should never be called, please check with the author if any problem.");
	}

	@Override
	public void write(DataOutput arg0) throws IOException {
		throw new UnsupportedOperationException(
		      "This method should never be called, please check with the author if any problem.");
	}

}