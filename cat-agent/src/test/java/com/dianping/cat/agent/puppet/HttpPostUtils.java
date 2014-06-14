package com.dianping.cat.agent.puppet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HttpPostUtils { 
    
    public static String httpPost(String urlAddress,Map<String, String> paramMap){ 
        if(paramMap==null){ 
            paramMap = new HashMap<String, String>(); 
        } 
        String [] params = new String[paramMap.size()]; 
        int i = 0; 
        for(String paramKey:paramMap.keySet()){ 
            String param = paramKey+"="+paramMap.get(paramMap); 
            params[i] = param; 
            i++; 
        } 
        return httpPost(urlAddress, params); 
    } 
    
    public static String httpPost(String urlAddress,List<String> paramList){ 
        if(paramList==null){ 
            paramList = new ArrayList<String>(); 
        } 
        return httpPost(urlAddress, paramList.toArray(new String[0])); 
    } 
    
    public static String httpPost(String urlAddress,String []params){ 
        URL url = null; 
        HttpURLConnection con  =null; 
        BufferedReader in = null; 
        StringBuffer result = new StringBuffer(); 
        try { 
            url = new URL(urlAddress); 
            con  = (HttpURLConnection) url.openConnection(); 
            con.setUseCaches(false); 
            con.setDoOutput(true); 
            con.setRequestMethod("POST"); 
            String paramsTemp = ""; 
            for(String param:params){ 
                if(param!=null&&!"".equals(param.trim())){ 
                    paramsTemp+="&"+param; 
                } 
            } 
            byte[] b = paramsTemp.getBytes(); 
            con.getOutputStream().write(b, 0, b.length); 
            con.getOutputStream().flush(); 
            con.getOutputStream().close(); 
            in = new BufferedReader(new InputStreamReader(con.getInputStream())); 
            while (true) { 
              String line = in.readLine(); 
              if (line == null) { 
                break; 
              } 
              else { 
                  result.append(line); 
              } 
            } 
        } catch (IOException e) { 
            e.printStackTrace(); 
        }finally{ 
            try { 
                if(in!=null){ 
                    in.close(); 
                } 
                if(con!=null){ 
                    con.disconnect(); 
                } 
            } catch (IOException e) { 
                e.printStackTrace(); 
            } 
        } 
        return result.toString(); 
    } 
} 
