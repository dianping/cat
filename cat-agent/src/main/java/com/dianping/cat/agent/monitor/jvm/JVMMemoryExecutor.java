package com.dianping.cat.agent.monitor.jvm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.DataEntity;
import com.dianping.cat.agent.monitor.Executor;

public class JVMMemoryExecutor implements Executor {

	public static String ID = "JVMMemoryExecutor";

	@Override
	public List<DataEntity> execute() {
		try {
			String pid = "";
			Process process = Runtime.getRuntime().exec("jstat -gcutil " + pid);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			reader.readLine();

			String output = reader.readLine();
			String[] metrics = output.split(" +");
			List<DataEntity> entities = new ArrayList<DataEntity>();

			DataEntity eden = new DataEntity();

			eden.setKey("jvm_edenUsage_").setOp("avg").setValue(Double.valueOf(metrics[2]) / 100);

			DataEntity old = new DataEntity();
			old.setKey("jvm_oldUsage_").setOp("avg").setValue(Double.valueOf(metrics[3]) / 100);

			DataEntity perm = new DataEntity();
			perm.setKey("jvm_permUsage_").setOp("avg").setValue(Double.valueOf(metrics[4]) / 100);

			return entities;
		} catch (Exception e) {
			Cat.logError(e);
		}

		return new ArrayList<DataEntity>();
	}

	@Override
   public String getId() {
		return ID;
	}

}
