package com.dianping.cat.abtest.repository;

import java.util.HashMap;
import java.util.Map;

import org.unidal.socket.Message;

public class ProtocolMessage implements Message {

	public static final String HELLO_NAME = "hello";

	public static final String HEARTBEAT_NAME = "heartbeat";

	private String m_name;

	private String m_content;

	private Map<String, String> m_headers = new HashMap<String, String>();

	public String getContent() {
		return m_content;
	}
	
	public void addHeader(String header, String value){
		m_headers.put(header,value);
	}

	public Map<String, String> getHeaders() {
		return m_headers;
	}

	public String getName() {
		return m_name;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setName(String name) {
		m_name = name;
	}

	@Override
	public String toString() {
		return String.format("ProtocolMessage[name=%s, content=%s, headers=%s]", m_name, m_content, m_headers);
	}
}