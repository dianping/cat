package com.dianping.cat.report.page.network.nettopology.model;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.unidal.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.dianping.cat.Cat;
import com.dianping.cat.report.page.JsonBuilder;
import com.dianping.cat.report.page.network.nettopology.DomOp;

public class NetGraph {
	private ArrayList<NetTopology> netTopologys;

	public NetGraph(String configFile) {
		netTopologys = new ArrayList<NetTopology>();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Node root;
		try {
			DocumentBuilder builder = dbf.newDocumentBuilder();
			InputStream in = NetTopology.class.getResourceAsStream(configFile);
			Document doc = builder.parse(in);
			root = (Node) doc.getDocumentElement();
		} catch (Exception e) {
			Cat.logError(e);
			return;
		}

		for (Node node : (new DomOp(root)).getChildNodes("nettopogy")) {
			netTopologys.add(new NetTopology(node));
		}
	}
	
	public ArrayList<Pair<String, String>> getJsonData() {
		ArrayList<Pair<String, String>> json = new ArrayList<Pair<String, String>>();
		
		for (NetTopology netTopology : netTopologys) {
			String name = netTopology.getName();
			String jsonData = new JsonBuilder().toJson(netTopology);
			json.add(new Pair<String, String>(name, jsonData));
		}
		
		return json;
	}

	public ArrayList<NetTopology> getNetTopologys() {
		return netTopologys;
	}

	public void setNetTopologys(ArrayList<NetTopology> netTopologys) {
		this.netTopologys = netTopologys;
	}
}
