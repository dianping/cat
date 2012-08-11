package com.dianping.cat.notify;
import java.io.IOException;
import java.text.DecimalFormat;

import org.junit.Test;

import com.dianping.cat.notify.CatNotifyServer;

public class CatMailServerTest {
	
	
	public void testForUser(){
		DecimalFormat floatFormat = new DecimalFormat(",###.##");
		DecimalFormat integerFormat = new DecimalFormat(",###");
		System.out.println(floatFormat.format(65465465441234.15654654654654));
		System.out.println(integerFormat.format(4545.15654654654654));
		
	}
	
	@Test
	public void test() {
		System.setProperty("spring.context", "cat-notify.xml");
		CatNotifyServer.main(null);
		try {
			System.in.read(); 
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void testHandler() {
		System.setProperty("spring.context", "processor.xml");
		
	} 
	
	/*@Override
	public void renderNode(String path,PrintWriter writer) {
		if(null == render){
	    	render = new TreeRender();
	    }
		List<INode> nodeList = getChildrenNodes(path);
		for(INode childNode:nodeList){
			render.display(childNode, writer);
		}
	}

	@Override
	public void setRender(IRender render) {
		this.render = render;
	}*/
}
