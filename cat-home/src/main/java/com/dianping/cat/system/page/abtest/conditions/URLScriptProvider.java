package com.dianping.cat.system.page.abtest.conditions;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.abtest.model.entity.Condition;

public class URLScriptProvider implements ScriptProvider {

	private String m_actual = "actual";

	@Override
	public List<String> actions() {
		List<String> actions = new ArrayList<String>();

		actions.add("Is equals to (case insens.)");
		actions.add("Is not equals to (case insens.)");
		actions.add("Is equals to (case sens.)");
		actions.add("Is not equals to (case sens.)");
		actions.add("Is equals to (case insens.)");
		actions.add("Marches Regex (case insens.)");
		actions.add("Marches Regex (case sens.)");
		actions.add("contains");
		actions.add("does not contain");

		return actions;
	}

	private String equalsByCaseInsens(String expectedUrl) {
		return String.format("%s.equalsIgnoreCase(\"%s\")", m_actual, expectedUrl);
	}

	private String notEqualsByCaseInsens(String expectedUrl) {
		return String.format("!%s.equalsIgnoreCase(\"%s\")", m_actual, expectedUrl);
	}

	private String equalsByCaseSens(String expectedUrl) {
		return String.format("%s.equals(\"%s\")", m_actual, expectedUrl);
	}

	private String notEqualsByCaseSens(String expectedUrl) {
		return String.format("!%s.equals(\"%s\")", m_actual, expectedUrl);
	}

	private String marcherByCaseInsens(String expectedUrl) {
		int pos = expectedUrl.indexOf('*');
		String subUrl = expectedUrl.substring(0, pos);

		return String.format("%s.toLowerCase().startsWith(\"%s\".toLowerCase())", m_actual, subUrl);
	}

	private String marcherByCaseSens(String expectedUrl) {
		int pos = expectedUrl.indexOf('*');
		
		if(pos > -1){
			String subUrl = expectedUrl.substring(0, pos);
			
			return String.format("%s.startsWith(\"%s\")", m_actual, subUrl);
		}else{
			return "false";
		}
	}
	
	private String contain(String expectedUrl){
		return String.format("%s.indexOf(\"%s\") > -1", m_actual, expectedUrl);
	}

	private String notContain(String expectedUrl){
		return String.format("%s.indexOf(\"%s\") == -1", m_actual, expectedUrl);
	}
	
	public String getFragement(Condition condition) {
		String expectedUrl = condition.getText();

		switch (condition.getComparator()) {
		case 1:
			return equalsByCaseInsens(expectedUrl);
		case 2:
			return notEqualsByCaseInsens(expectedUrl);
		case 3:
			return equalsByCaseSens(expectedUrl);
		case 4:
			return notEqualsByCaseSens(expectedUrl);
		case 5:
			return marcherByCaseInsens(expectedUrl);
		case 6:
			return marcherByCaseSens(expectedUrl);
		case 7:
			return contain(expectedUrl);
		case 8:
			return notContain(expectedUrl);
		}

		return "false";
	}
}
