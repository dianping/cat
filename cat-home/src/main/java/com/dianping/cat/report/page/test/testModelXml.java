package com.dianping.cat.report.page.test;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.home.animal.entity.*;
import com.dianping.cat.home.animal.transform.DefaultSaxParser;
import com.dianping.cat.home.animal.transform.DefaultXmlBuilder;
import org.junit.Assert;
/*
<animal>
<dog name="jack" size="big">
	<food name="chicken" time="morning"></food>
	<food name="bone" time="noon"></food>
	<food name="rice" time="evening"></food>
</dog>
<cat name="tom" size="medium">
	<food name="rice" time="morning"></food>
	<food name="fish" time="noon"></food>
	<food name="mouse" time="evening"></food>
</cat>
<mouse name="jerry" size="small">
	<food name="rice" time="morning"></food>
	<food name="bread" time="noon"></food>
	<food name="noodle" time="evening"></food>
</mouse>
</animal>
*/

public class testModelXml {
	
	public static void main(String[] args){
		System.out.println(check());
	}
	
	public static String check(){
		DefaultXmlBuilder builder = new DefaultXmlBuilder();
		Animal animal = getAnimal();
		String xmlString = builder.buildXml(animal);
		try {
	      Animal generatedAnimal = DefaultSaxParser.parse(xmlString);
	      Assert.assertEquals(animal, generatedAnimal);
      } catch (SAXException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      } catch (IOException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
		return xmlString;
	}

	
	public static Animal getAnimal(){
		Animal animal = new Animal();
		Cat cat = new Cat();
		cat.setName("tom");
		cat.setSize("medium");
		Dog dog = new Dog();
		dog.setName("jack");
		dog.setSize("big");
		Mouse mouse = new Mouse();
		mouse.setName("jerry");
		mouse.setSize("small");
		
		Food catFoodMorning = new Food();
		catFoodMorning.setName("rice");
		catFoodMorning.setTime("morning");
		
		Food catFoodNoon = new Food();
		catFoodNoon.setName("fish");
		catFoodNoon.setTime("noon");
		
		Food catFoodEvening = new Food();
		catFoodEvening.setName("mouse");
		catFoodEvening.setTime("evening");
		
		Food dogFoodMorning = new Food();
		dogFoodMorning.setName("chicken");
		dogFoodMorning.setTime("morning");
		
		Food dogFoodNoon = new Food();
		dogFoodNoon.setName("bone");
		dogFoodNoon.setTime("noon");
		Food dogFoodEvening = new Food();
		dogFoodEvening.setName("rice");
		dogFoodEvening.setTime("evening");
		
		Food mouseFoodMorning = new Food();
		mouseFoodMorning.setName("rice");
		mouseFoodMorning.setTime("morning");
		Food mouseFoodNoon = new Food();
		mouseFoodNoon.setName("bread");
		mouseFoodNoon.setTime("noon");
		Food mouseFoodEvening = new Food();
		mouseFoodNoon.setName("noodle");
		mouseFoodNoon.setTime("evening");
		
		dog.addFood(dogFoodMorning);
		dog.addFood(dogFoodNoon);
		dog.addFood(dogFoodEvening);
		
		cat.addFood(catFoodMorning);
		cat.addFood(catFoodNoon);
		cat.addFood(catFoodEvening);
		
		mouse.addFood(mouseFoodMorning);
		mouse.addFood(mouseFoodNoon);
		mouse.addFood(mouseFoodEvening);
		
		animal.setCat(cat);
		animal.setDog(dog);
		animal.setMouse(mouse);
		
		return animal;
	}
}
