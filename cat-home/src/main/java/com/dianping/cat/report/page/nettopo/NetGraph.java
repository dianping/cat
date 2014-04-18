package com.dianping.cat.report.page.nettopo;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.unidal.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


public class NetGraph {
	private ArrayList<NetTopology> netTopologys;
	
	private static NetGraph instance;
		
	private NetGraph() {
		netTopologys = new ArrayList<NetTopology>();
	}
	
	public static NetGraph getInstance() {
		if (instance == null) {
			instance = new NetGraph();
		}
		return instance;
	}  

	public ArrayList<NetTopology> getNetTopologys() {
		return netTopologys;
	}

	public void setNetTopologys(ArrayList<NetTopology> netTopologys) {
		this.netTopologys = netTopologys;
	}
	
	public synchronized void buildGraphFromConfig() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
		Node root;
        try {  
            DocumentBuilder builder = dbf.newDocumentBuilder();  
            InputStream in = NetTopology.class.getResourceAsStream("/config/netconfig.xml");
            Document doc = builder.parse(in);
            root = (Node)doc.getDocumentElement();   
        }
        catch (Exception e) {  
            e.printStackTrace(); 
            return;
        } 
        
        for (Node node : (new DomOp(root)).getChildNodes("nettopogy")){
        	netTopologys.add(new NetTopology(node));
        }
	}
	
	public synchronized ArrayList<Pair<String, String>> getJsonData() {
		ArrayList<Pair<String, String>> jsonData = new ArrayList<Pair<String, String>>();
		
		String name;
		StringBuilder json = new StringBuilder();
		double in, out;
		for (NetTopology netTopology : netTopologys) {
			name = netTopology.getName();
			
			json.delete(0, json.length());
			json.append("{");
			json.append("'anchor':{");
			for (Anchor anchor : netTopology.getAnchors()) {
				json.append("'");
				json.append(anchor.getName());
				json.append("':{'x':");
				json.append(Integer.toString(anchor.getX()));
				json.append(",'y':");
				json.append(Integer.toString(anchor.getY()));
				json.append("},");
			}
			json.append("},");
			
			json.append("'sw':{");
			for (Switch switch_ : netTopology.getSwitchs()) {
				json.append("'");
				json.append(switch_.getName());
				json.append("':{'x':");
				json.append(Integer.toString(switch_.getX()));
				json.append(",'y':");
				json.append(Integer.toString(switch_.getY()));
				json.append("},");
			}
			json.append("},");
			
			json.append("'conn':[");
			for (Connection connection : netTopology.getConnections()) {
				json.append("[");
				
				json.append("['");
				json.append(connection.getFirst());
				json.append("',");
				in = 0;
				for (Interface interface_ : connection.getFirstData()) {
					in += interface_.getIn();
				}
				json.append(Double.toString(in));
				json.append(",");
				out = 0;
				for (Interface interface_ : connection.getFirstData()) {
					out += interface_.getOut();
				}
				json.append(Double.toString(out));
				json.append(",");
				json.append("],");
				
				json.append("['");
				json.append(connection.getSecond());
				json.append("',");
				in = 0;
				for (Interface interface_ : connection.getSecondData()) {
					in += interface_.getIn();
				}
				json.append(Double.toString(in));
				json.append(",");
				out = 0;
				for (Interface interface_ : connection.getSecondData()) {
					out += interface_.getOut();
				}
				json.append(Double.toString(out));
				json.append(",");
				json.append("],");
				
				json.append("],");
			}
			json.append("],");
			json.append("}");
			
			jsonData.add(new Pair<String, String>(name, json.toString()));
		}
		
		return jsonData;
	}
}
