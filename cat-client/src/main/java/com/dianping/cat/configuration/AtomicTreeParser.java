package com.dianping.cat.configuration;

import java.util.ArrayList;
import java.util.List;

import org.unidal.helper.Splitters;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

public class AtomicTreeParser {

	private List<String> m_startTypes = new ArrayList<String>();

	private List<String> m_matchTypes = new ArrayList<String>();

	public void init(String startTypes, String matchTypes) {
		if (startTypes != null) {
			m_startTypes = Splitters.by(";").noEmptyItem().split(startTypes);
		}
		if (matchTypes != null) {
			m_matchTypes = Splitters.by(";").noEmptyItem().split(matchTypes);
		}
	}

	public boolean isAtomicMessage(MessageTree tree) {
		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			String type = message.getType();

			if (m_startTypes != null) {
				for (String s : m_startTypes) {
					if (type.startsWith(s)) {
						return true;
					}
				}
			}
			if (m_matchTypes != null) {
				return m_matchTypes.contains(type);
			}
			return false;
		} else {
			return true;
		}
	}
	
}
