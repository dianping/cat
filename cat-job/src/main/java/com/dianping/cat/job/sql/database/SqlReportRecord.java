package com.dianping.cat.job.sql.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SqlReportRecord {

	private String m_domain;
	
	private String m_name;

	private String m_statement;

	private String m_sampleLink;

	private Date m_transactionDate;

	private int m_totalCount;

	private int m_failureCount;

	private int m_longCount;

	private double m_min;

	private double m_max;

	/**
	 * the avg2 is not contain the max of top5%
	 */
	private double m_avg2;

	private double m_sum;

	private double m_sum2;

	private Date m_creationDate;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private SimpleDateFormat hourFormat = new SimpleDateFormat("yyyyMMdd/HH");

	private static final String SPIT = "\t";

	public SqlReportRecord(){
		
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(sdf.format(m_transactionDate)).append(SPIT);
		sb.append(sdf.format(m_creationDate)).append(SPIT);
		sb.append(m_domain).append(SPIT);
		sb.append(m_name).append(SPIT);
		sb.append(m_statement).append(SPIT);
		sb.append(m_totalCount).append(SPIT);
		sb.append(m_failureCount).append(SPIT);
		sb.append(m_longCount).append(SPIT);
		sb.append(m_min).append(SPIT);
		sb.append(m_max).append(SPIT);
		sb.append(m_sum).append(SPIT);
		sb.append(m_sum2).append(SPIT);
		sb.append(m_avg2).append(SPIT);
		sb.append(m_sampleLink).append(SPIT);
		return sb.toString();
	}

	// domain1 SQLStatement Internal9 500 500 500 100 199 74750 11591750 147
	public SqlReportRecord(String currentHour,String text) {
		
		try {
	      m_transactionDate = hourFormat.parse(currentHour);
      } catch (ParseException e) {
      	Date error = new Date();
      	error.setTime(0);
	      m_transactionDate =error ;
      }

		m_creationDate = new Date();
		String[] params = text.split("\t");
		m_domain = params[0];
		m_name = params[1];
		m_statement = params[2];
		m_totalCount = Integer.parseInt(params[3]);
		m_failureCount = Integer.parseInt(params[4]);
		m_longCount = Integer.parseInt(params[5]);
		m_min = Double.parseDouble(params[6]);
		m_max = Double.parseDouble(params[7]);
		m_sum = Double.parseDouble(params[8]);
		m_sum2 = Double.parseDouble(params[9]);
		m_avg2 = Double.parseDouble(params[10]);
		m_sampleLink = params[11];
	}

	
	public String getName() {
   	return m_name;
   }

	public void setName(String name) {
   	m_name = name;
   }

	public String getDomain() {
		return m_domain;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public String getStatement() {
		return m_statement;
	}

	public void setStatement(String statement) {
		m_statement = statement;
	}

	public String getSampleLink() {
		return m_sampleLink;
	}

	public void setSampleLink(String sampleLink) {
		m_sampleLink = sampleLink;
	}

	public Date getDate() {
		return m_transactionDate;
	}

	public void setDate(Date date) {
		m_transactionDate = date;
	}

	public int getTotalCount() {
		return m_totalCount;
	}

	public void setTotalCount(int totalCount) {
		m_totalCount = totalCount;
	}

	public int getFailureCount() {
		return m_failureCount;
	}

	public void setFailureCount(int failureCount) {
		m_failureCount = failureCount;
	}

	public int getLongCount() {
		return m_longCount;
	}

	public void setLongCount(int longCount) {
		m_longCount = longCount;
	}

	public double getMin() {
		return m_min;
	}

	public void setMin(double min) {
		m_min = min;
	}

	public double getMax() {
		return m_max;
	}

	public void setMax(double max) {
		m_max = max;
	}

	public double getAvg2() {
		return m_avg2;
	}

	public void setAvg2(double avg2) {
		m_avg2 = avg2;
	}

	public double getSum() {
		return m_sum;
	}

	public void setSum(double sum) {
		m_sum = sum;
	}

	public double getSum2() {
		return m_sum2;
	}

	public void setSum2(double sum2) {
		m_sum2 = sum2;
	}

	public Date getCreatTime() {
		return m_creationDate;
	}

	public void setCreatTime(Date creatTime) {
		m_creationDate = creatTime;
	}

}
