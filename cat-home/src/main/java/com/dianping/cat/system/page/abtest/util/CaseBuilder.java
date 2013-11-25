package com.dianping.cat.system.page.abtest.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.abtest.model.entity.Case;
import com.dianping.cat.abtest.model.entity.Condition;
import com.dianping.cat.abtest.model.entity.ConversionRule;
import com.dianping.cat.abtest.model.entity.GroupstrategyDescriptor;
import com.dianping.cat.abtest.model.entity.Run;
import com.dianping.cat.home.dal.abtest.Abtest;
import com.dianping.cat.home.dal.abtest.AbtestRun;
import com.dianping.cat.home.dal.abtest.GroupStrategy;
import com.dianping.cat.system.page.abtest.GsonBuilderManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class CaseBuilder {

	@Inject
	private GsonBuilderManager m_gsonBuilderManager;

	private Type m_listType = new TypeToken<ArrayList<Condition>>() {
	}.getType();

	private Type m_ruleType = new TypeToken<ArrayList<ConversionRule>>() {
	}.getType();

	public Case build(Abtest abtest, AbtestRun abtestRun, GroupStrategy groupStrategy) {
		if (abtest != null && abtestRun != null && groupStrategy != null) {
			Case case_ = buildCaseFromAbtest(abtest, groupStrategy);

			Run run = buildRunFromAbtestRun(abtestRun);

			case_.addRun(run);

			return case_;
		} else {
			throw new RuntimeException("Cannot builder case due to the abtest , abtestRun or groupStrategy is null");
		}
	}

	private Case buildCaseFromAbtest(Abtest abtest, GroupStrategy groupStrategy) {
		Case case_ = new Case(abtest.getId());

		case_.setCreatedDate(abtest.getCreationDate());
		case_.setDescription(abtest.getDescription());
		case_.setGroupStrategy(groupStrategy.getName());
		case_.setName(abtest.getName());
		case_.setOwner(abtest.getOwner());
		case_.setLastModifiedDate(abtest.getModifiedDate());

		for (String domain : StringUtils.split(abtest.getDomains(), ',')) {
			case_.addDomain(domain);
		}

		return case_;
	}

	private Run buildRunFromAbtestRun(AbtestRun abtestRun) {
		Run run = new Run(abtestRun.getId());
		Gson gson = m_gsonBuilderManager.getGsonBuilder().create();

		for (String domain : StringUtils.split(abtestRun.getDomains(), ',')) {
			run.addDomain(domain);
		}
		run.setCreator(abtestRun.getCreator());
		run.setConditionsFragement(abtestRun.getJavaFragement());
		run.setDisabled(false);
		run.setEndDate(abtestRun.getEndDate());
		run.setStartDate(abtestRun.getStartDate());
		run.setCreatedDate(abtestRun.getCreationDate());
		run.setLastModifiedDate(abtestRun.getModifiedDate());

		if (StringUtils.isNotBlank(abtestRun.getStrategyConfiguration())) {
			run.setGroupstrategyDescriptor(gson.fromJson(abtestRun.getStrategyConfiguration(),
			      GroupstrategyDescriptor.class));
		}

		if (StringUtils.isNotBlank(abtestRun.getConditions())) {
			List<Condition> conditions = gson.fromJson(abtestRun.getConditions(), m_listType);

			run.getConditions().addAll(conditions);
		}

		if (StringUtils.isNotBlank(abtestRun.getConversionGoals())) {
			List<ConversionRule> conversions = gson.fromJson(abtestRun.getConversionGoals(), m_ruleType);

			run.getConversionRules().addAll(conversions);
		}

		return run;
	}
}
