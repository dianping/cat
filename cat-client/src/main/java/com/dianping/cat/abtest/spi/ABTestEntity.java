package com.dianping.cat.abtest.spi;

import java.util.Date;
import java.util.List;

import javax.script.Invocable;

import com.dianping.cat.abtest.model.entity.Case;
import com.dianping.cat.abtest.model.entity.Condition;
import com.dianping.cat.abtest.model.entity.ConversionRule;
import com.dianping.cat.abtest.model.entity.GroupstrategyDescriptor;
import com.dianping.cat.abtest.model.entity.Run;
import com.dianping.cat.abtest.spi.internal.ABTestCodec;
import com.dianping.cat.message.spi.MessageManager;

public class ABTestEntity {

	private String m_name;

	private Run m_run;

	private ABTestGroupStrategy m_groupStrategy;
	
	private Invocable m_invocable;
	
	private MessageManager m_messageManager;
	
	private ABTestCodec m_cookieCodec;

	private String m_groupStrategyName;

	public ABTestEntity() {
		m_run = new Run();
		m_run.setDisabled(true);
	}

	public ABTestEntity(Case _case, Run run) {
		m_name = _case.getName();
		m_groupStrategyName = _case.getGroupStrategy();
		m_run = run;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ABTestEntity other = (ABTestEntity) obj;
		if (m_groupStrategy == null) {
			if (other.m_groupStrategy != null)
				return false;
		} else if (!m_groupStrategy.equals(other.m_groupStrategy))
			return false;
		if (m_groupStrategyName == null) {
			if (other.m_groupStrategyName != null)
				return false;
		} else if (!m_groupStrategyName.equals(other.m_groupStrategyName))
			return false;
		if (m_name == null) {
			if (other.m_name != null)
				return false;
		} else if (!m_name.equals(other.m_name))
			return false;
		if (m_run == null) {
			if (other.m_run != null)
				return false;
		} else if (!m_run.equals(other.m_run))
			return false;
		return true;
	}

	public List<Condition> getConditions() {
		return m_run.getConditions() != null ? m_run.getConditions() : null;
	}

	public String getConditionsFragement() {
		return m_run.getConditionsFragement();
	}

	public List<ConversionRule> getConversionRules() {
		return m_run.getConversionRules();
	}

	public ABTestCodec getCookieCodec() {
   	return m_cookieCodec;
   }

	public Date getEndDate() {
		return m_run.getEndDate();
	}

	public ABTestGroupStrategy getGroupStrategy() {
		return m_groupStrategy;
	}

	public GroupstrategyDescriptor getGroupStrategyDescriptor() {
		return m_run.getGroupstrategyDescriptor() != null ? m_run.getGroupstrategyDescriptor() : null;
	}

	public String getGroupStrategyName() {
		return m_groupStrategyName != null ? m_groupStrategyName : null;
	}

	public Invocable getInvocable() {
   	return m_invocable;
   }

	public MessageManager getMessageManager() {
   	return m_messageManager;
   }

	public String getName() {
		return m_name;
	}

	public Run getRun() {
		return m_run;
	}

	public Date getStartDate() {
		return m_run.getStartDate();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_groupStrategy == null) ? 0 : m_groupStrategy.hashCode());
		result = prime * result + ((m_groupStrategyName == null) ? 0 : m_groupStrategyName.hashCode());
		result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
		result = prime * result + ((m_run == null) ? 0 : m_run.hashCode());
		return result;
	}
	
	public boolean isDisabled() {
		return m_run.isDisabled();
	}

	public boolean isEligible(Date date) {
		if (m_run.isDisabled()) {
			return false;
		}

		Date startDate = m_run.getStartDate();
		if (startDate != null) {
			if (date.before(startDate)) {
				return false;
			}
		}

		Date endDate = m_run.getEndDate();
		if (endDate != null) {
			if (date.after(endDate)) {
				return false;
			}
		}

		return true;
	}

	public void setCookieCodec(ABTestCodec cookieCodec) {
   	m_cookieCodec = cookieCodec;
   }

	public void setDisabled(boolean disabled) {
		m_run.setDisabled(disabled);
	}

	public void setGroupStrategy(ABTestGroupStrategy groupStrategy) {
		m_groupStrategy = groupStrategy;
	}

	public void setInvocable(Invocable invocable) {
   	m_invocable = invocable;
   }
	
	public void setMessageManager(MessageManager messageManager) {
   	m_messageManager = messageManager;
   }

	public void setName(String name) {
		m_name = name;
	}

	@Override
	public String toString() {
		return "ABTestEntity [m_name=" + m_name + ", m_groupStrategyName=" + m_groupStrategyName + ", m_run=" + m_run
		      + ", m_groupStrategy=" + m_groupStrategy + "]";
	}
}