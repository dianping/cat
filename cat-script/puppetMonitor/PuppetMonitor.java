package puppetMonitor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.*;
import java.util.Calendar;
import java.net.*; 


public class PuppetMonitor {

	private static String m_logFile;
	private static String m_lineFile;

	private static void run() {
		boolean active = true;
		Long end_position=0L;
		while (active) {
			Long position = getReaderPostion(m_lineFile);
			try{
				RandomAccessFile reader = new RandomAccessFile(m_logFile, "r");
				reader.seek(position);
				String line=null;
				while((line=reader.readLine())!=null){
					Alertation alertation=parse(line);
					if(alertation.getDomain() != null){
						sendHttp(alertation);
//						break;
					}
				}
				end_position = reader.getFilePointer();
			}catch(IOException e){
				System.out.println("读文件异常:"+m_logFile);
				e.printStackTrace();
			}finally{
				setReaderPostion(m_lineFile,end_position);
			}
			try {
				Thread.sleep(1000 * 5);
			} catch (InterruptedException e) {
				break;
			}
		}
}

	private static void sendHttp(Alertation alertation) {
		String[] pars= new String[10];
		
		pars[0]="type="+alertation.getType();
		pars[1]="title="+alertation.getTitle();
		pars[2]="domain="+alertation.getDomain();
		pars[3]="ip="+alertation.getIp();
		pars[4]="user="+alertation.getUser();
		pars[5]="content="+alertation.getContent();
		pars[6]="url="+alertation.getUrl();
		pars[7]="op="+alertation.getOp();
		pars[8]="date="+alertation.getDate();
		pars[9]="hostname="+alertation.getHostname();
		String url="http://10.128.120.12:2281/cat/r/alteration";
		HttpPostUtils httppost=new HttpPostUtils();
		httppost.setUrlAddress(url);
		System.out.println(httppost.httpPost(pars));
//		System.out.println(alertation.getDate()+";"+alertation.getDomain()+";"+alertation.getHostname()+";"+alertation.getIp()+";"+alertation.getTitle()+";"+alertation.getContent());
	}

	private static Alertation parse(String line) {
		String type="puppet";
		String user="puppet";
		String url="";
		String op="insert";
		String host="";
		String IP="";
		String date="";
		String domain="";
		String content="";
		String title="";  
		String regEx = ".*puppet-agent.*\\(\\/Stage";
		Alertation alertation = new Alertation();
		if (Pattern.compile(regEx).matcher(line).find()) {
			String[] tmp_list = line.split(" ");
			Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			
			date = tmp_list[0] + tmp_list[1]+ "  " + tmp_list[2] + " "+ tmp_list[3]+ " " +Integer.toString(year);
			String all_content=line.split("\\(")[1];
			title=all_content.split("\\)")[0].split("\\[main\\]\\/")[1];
			content=all_content.split("\\)")[1];
			
			SimpleDateFormat sdf = new SimpleDateFormat("MMMM  dd HH:mm:ss yyyy",Locale.US);
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);			
			try {
			date=sdf2.format(sdf.parse(date));
			} catch (ParseException e1) {
			e1.printStackTrace();
			}
			
//			System.out.println(date);
			try{
				InetAddress ia = InetAddress.getLocalHost(); 
				host = ia.getHostName();
				IP= ia.getHostAddress();
				domain=host.split("-sl-|-gp-|-ppe")[0];				
			}catch(UnknownHostException e){  
                e.printStackTrace();  
			} 
			alertation.setDate(date);
			alertation.setHostname(host);
			alertation.setIp(IP);
			alertation.setDomain(domain);
			alertation.setTitle(title);
			alertation.setContent(content);
			alertation.setOp(op);
			alertation.setUrl(url);
			alertation.setUser(user);
			alertation.setType(type);	
		}
        return alertation;
	}

//	private String getNextLine(int position) {
//		return null;
//	}
/**
 * 
 * @param line_file,记录文件读取位置的文件
 * @return 记录的数据，否则返回0 
 * 读取文件失败的时候是否创建文件line_file
 * 
 */
	private static long getReaderPostion(String line_file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(line_file));
			String str = reader.readLine();
			if (str != null) {
				return Long.parseLong(str);
			}
		}catch(FileNotFoundException e1){
			File filename = new File(line_file);
			try {
				filename.createNewFile();
			} catch (IOException e2) {
				System.out.println("创建文件失败:" + line_file);
				e2.printStackTrace();
			}	
		} catch (Exception e3) {
			e3.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return 0L;
	}
	
	private static void setReaderPostion(String line_file,Long end_position) {
		try{
			BufferedWriter output = new BufferedWriter(new FileWriter(line_file));
			output.write(Long.toString(end_position));
			output.close();
		}catch(IOException e){
			System.out.println("写入文件异常：line_file");
			e.printStackTrace();
		}
	}
	

	public static void main(String args[]) {
//		if (args.length > 2) {
//			m_logFile = args[0];
//			m_lineFile = args[1];
//		} else {
//			System.out.println("Please check pars!");
//			System.exit(0);
//		}

		m_logFile = "/Users/River/messages";
		m_lineFile = "/var/log/line_random.log";
		run();
	}
}
