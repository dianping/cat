package com.dianping.cat.agent.monitor.puppet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.dianping.cat.Cat;

public class ReaderManager {

	private static final String POINTER_FILE = "/var/log/currentPointer";

	public long queryPointer() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(POINTER_FILE));
			String str = reader.readLine();
			if (str != null) {
				return Long.parseLong(str);
			} else {
				return 0L;
			}
		} catch (FileNotFoundException e1) {
			File filename = new File(POINTER_FILE);
			try {
				filename.createNewFile();
			} catch (IOException e2) {
				Cat.logError("创建文件失败:" + POINTER_FILE, e2);
			}
		} catch (Exception e3) {
			Cat.logError(e3);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Cat.logError(e);
				}
			}

		}
		return 0L;
	}

	public void updatePointer(long end_position) {
		BufferedWriter output = null;
		try {
			output = new BufferedWriter(new FileWriter(POINTER_FILE));
			output.write(Long.toString(end_position));
			output.close();

		} catch (IOException e) {
			Cat.logError("写入文件异:" + POINTER_FILE, e);
		}
	}

}
