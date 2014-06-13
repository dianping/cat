package com.dianping.cat.agent.monitor.puppet;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.agent.monitor.puppet.util.CreatDir;
import com.dianping.cat.agent.monitor.puppet.util.GetReaderPostion;
import com.dianping.cat.agent.monitor.puppet.util.Parse;
import com.dianping.cat.agent.monitor.puppet.util.RunSysCmd;
import com.dianping.cat.agent.monitor.puppet.util.SendHttp;
import com.dianping.cat.agent.monitor.puppet.util.SetReaderPostion;

public class PuppetTask implements Task, Initializable {

	private static String m_logFile;

	private static String m_lineFile;

	private static Logger puppetLogger = Logger.getLogger("myLogger");

	@Override
	public void run() {
		SendHttp sendhttp = new SendHttp();
		Parse parse = new Parse();
		GetReaderPostion getreaderpostion = new GetReaderPostion();
		SetReaderPostion setreaderpostion = new SetReaderPostion();
		boolean active = true;
		Long end_position = 0L;
		RunSysCmd runsyscmd = new RunSysCmd();

		while (active) {
			Alertation alertation = null;
			Long position = getreaderpostion.getReaderPostion(m_lineFile);
			RandomAccessFile reader = null;
			try {
				reader = new RandomAccessFile(m_logFile, "r");
				reader.seek(position);
				// 判断日志是否切割了,一定要放在while((line=reader.readLine())!=null)之前，否则回导致反复读取
				if (position >= 2) {
					reader.seek(position - 2);
					try {
						reader.readChar();
						reader.seek(position);
					} catch (IOException e) {
						setreaderpostion.setReaderPostion(m_lineFile, 0L);
						reader.seek(0L);
						puppetLogger.error(e.getMessage(), e);
					}
				}

				String line = null;
				while ((line = reader.readLine()) != null) {
					alertation = parse.parse(line);
					if (alertation != null) {
						sendhttp.sendHttp(alertation);
					} else {
						continue;
					}
				}
				end_position = reader.getFilePointer();

			} catch (IOException e) {
				puppetLogger.error("读文件异常:" + m_logFile);
				puppetLogger.error(e.getMessage(), e);
			} finally {
				if (end_position > position) {
					setreaderpostion.setReaderPostion(m_lineFile, end_position);
				}
				try {
					reader.close();
				} catch (IOException e) {
					puppetLogger.error(e.getMessage(), e);
				}
				puppetLogger.info("本次读取的开始偏移量:" + position + " 末尾偏移量:" + end_position);
			}
			try {
				Thread.sleep(1000 * 5);
			} catch (InterruptedException e) {
				puppetLogger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_logFile = "/var/log/messages";
		m_lineFile = "/var/log/line_random.log";

		CreatDir creatdir = new CreatDir();
		creatdir.creatDir("/data/applogs/monitor");
		PropertyConfigurator.configure("log4j.properties");
//		Threads.forGroup("Cat").start(this);
	}

	@Override
	public String getName() {
		return "puppet";
	}

	@Override
	public void shutdown() {

	}
}
