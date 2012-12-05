package com.dianping.cat.job.sql;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

public class UrlStatementKey implements WritableComparable<UrlStatementKey> {
	private Text m_name;

	private Text m_domain;

	private Text m_statement;

	public UrlStatementKey() {
		m_name = new Text();
		m_statement = new Text();
		m_domain = new Text();
	}

	public Text getName() {
		return m_name;
	}

	public UrlStatementKey setName(Text name) {
		m_name = name;
		return this;
	}

	public Text getDomain() {
		return m_domain;
	}

	public UrlStatementKey setDomain(Text domain) {
		this.m_domain = domain;
		return this;
	}

	public Text getStatement() {
		return m_statement;
	}

	public UrlStatementKey setStatement(Text statement) {
		this.m_statement = statement;
		return this;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		m_name.write(out);
		m_domain.write(out);
		m_statement.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		m_name.readFields(in);
		m_domain.readFields(in);
		m_statement.readFields(in);
	}

	@Override
	public int compareTo(UrlStatementKey key) {
		if (m_domain.toString().equals(key.getDomain().toString())) {
			if (m_name.toString().equals(key.getName().toString())) {
				return 0;
			} else {
				return m_name.compareTo(key.getName());
			}
		}
		return m_domain.compareTo(key.getDomain());
	}

	@Override
	public String toString() {
		// to assure the output string not contain \t
		String statement = m_statement.toString();
		m_statement = new Text(replaceBlank(statement));
		String name = m_name.toString();
		m_name = new Text(replaceBlank(name));
		return String.format("%s\t%s\t%s", m_domain, m_name, m_statement);
	}

	public String replaceBlank(String str) {
		String name = str.toString();
		name = name.replaceAll("\\s", " ");
		name = name.replaceAll("\t", " ");
		name = name.replaceAll("\"", "\'");
		return name;
	}
}
