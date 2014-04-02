package com.dianping.cat.home.menu;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.dianping.cat.home.menu.entity.Breakfast;
import com.dianping.cat.home.menu.entity.Dessert;
import com.dianping.cat.home.menu.entity.Lunch;
import com.dianping.cat.home.menu.entity.Menu;
import com.dianping.cat.home.menu.entity.Supper;
import com.dianping.cat.home.menu.transform.DefaultDomParser;
import com.dianping.cat.home.menu.transform.DefaultXmlBuilder;

public class MenuModelTest {
	@Test
	public void testXmlTransfer() throws Exception {
		DefaultXmlBuilder xmlBuilder = new DefaultXmlBuilder();
		Menu sourceMenu = generateMenu();
		String xmlContent = xmlBuilder.buildXml(sourceMenu);
		Menu targetMenu = new DefaultDomParser().parse(xmlContent);
		
		Assert.assertEquals("Transform error!", sourceMenu, targetMenu);
	}
	
	private Menu generateMenu(){
		Menu menu = new Menu();
		Breakfast breakfast = new Breakfast();
		Lunch lunch = new Lunch();
		Supper supper = new Supper();
		Dessert lunchDessert = new Dessert();
		Dessert supperDessert = new Dessert();
		
		menu.setDesc("this is munu");
		breakfast.setDesc("this is breakfast");
		lunch.setDesc("this is lunch");
		supper.setDesc("this is supper");
		lunchDessert.setDesc("this is lunchDessert");
		supperDessert.setDesc("this is supperDessert");
		
		menu.setBreakfast(breakfast);
		menu.setLunch(lunch);
		menu.setSupper(supper);
		lunch.setDessert(lunchDessert);
		supper.setDessert(supperDessert);
		
		return menu;
	}
}
