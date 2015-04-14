package com.dianping.cat.demo;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.helper.Urls;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.util.StringUtils;
import org.unidal.webres.json.JsonArray;
import org.unidal.webres.json.JsonObject;

import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.configuration.app.entity.Command;

public class AppDataFetcher extends ComponentTestCase {

	private String format = "http://cat.dianpingoa.com/cat/r/app?op=piechartJson&query1=2015-03-15;%s;;;;1;;;;00:00;23:59&groupByField=code&domains=All";

	@Test
	public void test() {
		AppConfigManager manager = lookup(AppConfigManager.class);
		Map<Integer, Command> rawCommands = manager.getRawCommands();
		Map<Integer, Code> codes = manager.getConfig().getCodes();

		for (Command command : rawCommands.values()) {
			for (Code code : command.getCodes().values()) {
				if (!codes.containsKey(code.getId())) {
					codes.put(code.getId(), code);
				}
			}
		}
		Map<Integer, String> codeMap = new LinkedHashMap<Integer, String>();

		System.out.print("命令字, 项目, 标题, ");
		for (Code code : codes.values()) {
			codeMap.put(code.getId(), code.getName());
		}
		for (Entry<Integer, String> code : codeMap.entrySet()) {
			System.out.print(code.getValue() + ", ");
		}
		System.out.print("\n");

		for (Command command : rawCommands.values()) {
			try {
				String url = String.format(format, command.getId());
				InputStream input = Urls.forIO().connectTimeout(5000).openStream(url);
				String result = Files.forIO().readFrom(input, "utf-8");
				JsonObject obj = new JsonObject(result);
				JsonArray piechart = obj.getJSONArray("pieChartDetails");
				Map<Integer, BigDecimal> datas = new HashMap<Integer, BigDecimal>();

				for (int i = 0; i < piechart.length(); i++) {
					JsonObject detail = new JsonObject(piechart.getString(i));
					int id = Integer.parseInt(detail.get("id").toString());
					double value = Double.parseDouble(detail.get("requestSum").toString());
					BigDecimal bigValue = new BigDecimal(value);
					datas.put(id, bigValue);
				}
				String domain = command.getDomain();
				if (StringUtils.isEmpty(domain) || domain.equals("0")) {
					domain = "无";
				}
				String title = command.getTitle();
				if (StringUtils.isEmpty(title) || title.equals("0")) {
					title = "无";
				}
				System.out.print(command.getName() + ", " + domain + ", " + title + ", ");

				for (Entry<Integer, String> code : codeMap.entrySet()) {
					BigDecimal value = datas.get(code.getKey());
					if (value == null) {
						value = new BigDecimal(0);
					}
					System.out.print(value + ", ");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.print("\n");
		}
	}
}
