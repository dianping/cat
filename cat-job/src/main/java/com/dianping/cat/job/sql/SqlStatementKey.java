package com.dianping.cat.job.sql;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

public class SqlStatementKey implements WritableComparable<SqlStatementKey> {
	private Text m_statement;

	private Text m_domain;

	public SqlStatementKey() {
		m_statement = new Text();
		m_domain = new Text();
	}

	public Text getDomain() {
		return m_domain;
	}

	public SqlStatementKey setDomain(Text domain) {
		this.m_domain = domain;
		return this;
	}

	public Text getStatement() {
		return m_statement;
	}

	public SqlStatementKey setStatement(Text statement) {
		this.m_statement = statement;
		return this;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		m_domain.write(out);
		m_statement.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		m_domain.readFields(in);
		m_statement.readFields(in);
	}

	@Override
	public int compareTo(SqlStatementKey key) {
		if (m_domain.toString().equals(key.getDomain().toString())) {
			if (m_statement.toString().equals(key.getStatement().toString())) {
				return 0;
			} else {
				return m_statement.compareTo(key.getStatement());
			}
		}
		return m_domain.compareTo(key.getDomain());
	}

	@Override
	public String toString() {
		return String.format("[domain:%s statement:%s]", m_domain, m_statement);
	}
}
