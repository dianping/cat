package com.dianping.cat.message.io;

import java.text.ParseException;
import java.util.Iterator;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.property.entity.Property;
import com.dianping.cat.configuration.property.entity.PropertyConfig;
import com.dianping.cat.util.json.JsonObject;

public class ClientMessage {

	private final int m_protocolId;

	private final int m_version;

	private final byte[] m_data;

	/**
		* "catc" -> 0x63617463 -> 1667331171
		*/
	public final static int PROTOCOL_ID = 1667331171;

	public final static int VERSION_0 = 0;

	public static void main(String[] args) {
		byte[] bytes = "catc".getBytes();
		int ret = 0;
		for (int i = 0; i < 4; i++) {
			ret <<= 8;
			ret |= bytes[i] & 0xFF;
		}
		System.out.println(ret);
	}

	public ClientMessage(int protocolId, int version, byte[] data) {
		m_protocolId = protocolId;
		m_version = version;
		m_data = data;
	}

	public ClientMessage(int protocolId, int version, String data) {
		m_protocolId = protocolId;
		m_version = version;
		m_data = data.getBytes();
	}

	public byte[] getData() {
		return m_data;
	}

	public int getProtocolId() {
		return m_protocolId;
	}

	public int getVersion() {
		return m_version;
	}

	public PropertyConfig toPropertyConfig() {
		JsonObject object;
		try {
			object = new JsonObject(new String(m_data));
		} catch (ParseException e) {
			Cat.logError(e);
			return null;
		}

		JsonObject kvs = object.getJSONObject("kvs");
		if (kvs == null) {
			return null;
		}

		PropertyConfig config = new PropertyConfig();
		for (Iterator<String> it = kvs.keys(); it.hasNext(); ) {
			String key = it.next();
			String val = kvs.getString(key);
			if (val != null) {
				config.addProperty(new Property().setId(key).setValue(val));
			}
		}
		return config;
	}
}
