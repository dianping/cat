/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-17
 * 
 * Copyright 2012 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.bee.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alibaba.cobar.config.loader.xml.XMLServerLoader;
import com.alibaba.cobar.config.util.ConfigException;
import com.alibaba.cobar.config.util.ConfigUtil;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class WhaleServerConfig {

	private static final int DEFAULT_WHALE_PORT = 7066;

	private int whalePort;

	public WhaleServerConfig() {
		whalePort = DEFAULT_WHALE_PORT;
	}

	public int getWhalePort() {
		return this.whalePort;
	}

	public void load() {
		InputStream dtd = null;
		InputStream xml = null;
		try {
			dtd = XMLServerLoader.class.getResourceAsStream("/server.dtd");
			xml = XMLServerLoader.class.getResourceAsStream("/server.xml");
			Element root = ConfigUtil.getDocument(dtd, xml).getDocumentElement();
			NodeList list = root.getElementsByTagName("system");
			for (int i = 0, n = list.getLength(); i < n; i++) {
				Node node = list.item(i);
				if (node instanceof Element) {
					Map<String, Object> props = ConfigUtil.loadElements((Element) node);
					if (props.containsKey("whale")) {
						this.whalePort = Integer.parseInt(props.get("whale").toString());
						break;
					}
				}
			}
		} catch (ConfigException e) {
			throw e;
		} catch (Throwable e) {
			throw new ConfigException(e);
		} finally {
			if (dtd != null) {
				try {
					dtd.close();
				} catch (IOException e) {
				}
			}
			if (xml != null) {
				try {
					xml.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
