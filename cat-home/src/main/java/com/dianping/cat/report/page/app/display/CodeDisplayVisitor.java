package com.dianping.cat.report.page.app.display;

import java.util.Arrays;
import java.util.List;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Constants;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.app.entity.Code;
import com.dianping.cat.home.app.entity.Command;
import com.dianping.cat.home.app.entity.Transaction;
import com.dianping.cat.home.app.transform.BaseVisitor;
import com.dianping.cat.service.ProjectService;

public class CodeDisplayVisitor extends BaseVisitor {

	private DisplayCommands m_commands = new DisplayCommands();

	private int m_currentCommand;

	private int[] m_distributions = new int[20];

	public static final List<Integer> STANDALONES = Arrays.asList(450);

	private ProjectService m_projectService;

	private AppConfigManager m_appConfigManager;

	public CodeDisplayVisitor(ProjectService projectService, AppConfigManager appConfigManager) {
		m_projectService = projectService;
		m_appConfigManager = appConfigManager;
		init();
	}

	private void buildDistributionInfo(Code code, String id) {
		DisplayCode c = m_commands.findOrCreateCommand(m_currentCommand).findOrCreateCode(id);

		c.incCount(code.getCount()).incErrors(code.getErrors()).incSum(code.getSum());
		long count = c.getCount();

		if (count > 0) {
			c.setAvg(c.getSum() / count);
			c.setSuccessRatio(100.0 - c.getErrors() * 100.0 / count);
		}
		String title = c.getTitle();

		if (StringUtils.isEmpty(title)) {
			title = "";
		}
		StringBuilder sb = new StringBuilder(title);
		sb.append(code.getId() + "=" + code.getCount() + "; ");

		c.setTitle(sb.toString());
	}

	private String convertLable(int i) {
		String code = String.valueOf(i);

		return code.replaceAll("0", "X");
	}

	public DisplayCommands getCommands() {
		return m_commands;
	}

	private void init() {
		for (int i = 1000; i >= 100; i -= 100) {
			m_distributions[10 - i / 100] = i;
		}
		for (int i = 10; i < 20; i++) {
			m_distributions[i] = -m_distributions[i - 10];
		}
	}

	private void mergeCode(Code code, String id) {
		DisplayCode c = m_commands.findOrCreateCommand(m_currentCommand).findOrCreateCode(id);

		c.incCount(code.getCount()).incErrors(code.getErrors()).incSum(code.getSum());

		long count = c.getCount();
		if (count > 0) {
			c.setAvg(c.getSum() / count);
			c.setSuccessRatio(100.0 - c.getErrors() * 100.0 / count);
		}
	}

	private void mergeCommand(Command command) {
		int id = command.getId();
		DisplayCommand c = m_commands.findOrCreateCommand(id);

		if (AppConfigManager.ALL_COMMAND_ID == id) {
			c.setName(Constants.ALL);
			c.setDomain(Constants.ALL);
			c.setTitle(Constants.ALL);
			c.setDepartment(Constants.ALL);
			c.setBu(Constants.ALL);
		} else {
			c.setName(command.getName());

			com.dianping.cat.configuration.app.entity.Command cmd = m_appConfigManager.getRawCommands().get(id);
			if (cmd != null) {
				c.setTitle(cmd.getTitle());
				String domain = cmd.getDomain();

				if (StringUtils.isNotEmpty(domain)) {
					Project project = m_projectService.findProject(domain);

					c.setDomain(domain);
					if (project != null) {
						c.setBu(project.getBu());
						c.setDepartment(project.getCmdbProductline());
					}
				}
			}
		}
		c.incCount(command.getCount()).incSum(command.getSum()).incErrors(command.getErrors())
		      .incRequestSum(command.getRequestSum()).incResponseSum(command.getResponseSum());

		long count = c.getCount();
		if (count > 0) {
			c.setAvg(command.getSum() / count);
			c.setSuccessRatio(100.0 - c.getErrors() * 100.0 / count);
			c.setRequestAvg(c.getRequestSum() * 1.0 / count);
			c.setResponseAvg(c.getResponseSum() * 1.0 / count);
		}

		Transaction transaction = command.getTransaction();

		if (transaction != null) {
			c.setTransactionCount(transaction.getCount());
			c.setTransactionAvg(transaction.getAvg());

			if (count > 0) {
				c.setCountComparison((c.getTransactionCount() - count) * 1.0 / count * 100);
			} else {
				c.setCountComparison(100);
			}

			double avg = c.getAvg();
			if (avg > 0) {
				c.setAvgComparison((c.getTransactionAvg() - avg) / avg * 100);
			} else {
				c.setAvgComparison(100);
			}
		}
	}

	private String queryCodeDistribution(int code) {
		if (!STANDALONES.contains(code)) {
			if (code >= 0 && code < 100) {
				return "0XX";
			} else if (code > -100 && code < 0) {
				return "-0XX";
			} else {
				for (int i = 0; i < m_distributions.length; i++) {
					if (code / m_distributions[i] >= 1) {
						return convertLable(m_distributions[i]);
					}
				}
			}
		}
		return String.valueOf(code);
	}

	@Override
	public void visitCode(Code code) {
		String id = code.getId();
		String distCode = queryCodeDistribution(Integer.valueOf(id));

		if (!id.equals(distCode)) {
			buildDistributionInfo(code, distCode);
		}
		mergeCode(code, id);
		super.visitCode(code);
	}

	@Override
	public void visitCommand(Command command) {
		m_currentCommand = command.getId();
		mergeCommand(command);

		super.visitCommand(command);
	}

}
