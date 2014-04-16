package puppetMonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpPostUtils {
	public String getUrlAddress() {
		return urlAddress;
	}

	public void setUrlAddress(String urlAddress) {
		this.urlAddress = urlAddress;
	}

	private String urlAddress;
	
    public String httpPost(String []params){ 
    	String flag="error";
        URL url = null; 
        HttpURLConnection con  =null; 
        BufferedReader in = null; 
        StringBuffer result = new StringBuffer(); 
        try { 
            url = new URL(this.urlAddress); 
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
            //test===============
//            System.out.println(paramsTemp);
            //test===============
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
        String rs=result.toString();
        if(rs.contains("200")){
        	flag="ok";
        }
        return flag; 
    }

}
