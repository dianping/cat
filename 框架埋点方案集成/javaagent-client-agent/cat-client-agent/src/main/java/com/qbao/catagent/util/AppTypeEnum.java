/**
 * 
 */
package com.qbao.catagent.util;

/**
 * 应用类型，现只支持tomcat,jetty,springboot
 * @author andersen
 *
 */
public enum AppTypeEnum {
	
	TOMCAT("tomcat"), JETTY("jetty"), SPRINGBOOT("springboot");
	
	private String name;
	
	private AppTypeEnum(String name){
		this.name = name;
	}
	
	 public String getName() {
         return name;
     }

     public void setName(String name) {
         this.name = name;
     }
     
     public static AppTypeEnum getAppType(String type){
    	 for (AppTypeEnum at : AppTypeEnum.values()){
    		 if (at.getName().equals(type)){
    			 return at;
    		 }
    	 }
    	 return null; 
     }
}
