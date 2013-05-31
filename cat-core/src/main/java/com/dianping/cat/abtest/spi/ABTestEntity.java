package com.dianping.cat.abtest.spi;

import java.util.Date;

import com.dianping.cat.abtest.model.entity.Case;
import com.dianping.cat.abtest.model.entity.Run;

public class ABTestEntity {

	private int m_id;
	
	private String m_name;

	private String m_groupStrategyName;

	private Run m_run;

	private ABTestGroupStrategy m_groupStrategy;

	public ABTestEntity() {
		m_run = new Run();
		m_run.setDisabled(true);
	}

	public ABTestEntity(Case _case, Run run) {
		m_id = _case.getId();
		m_name = _case.getName();
		m_groupStrategyName = _case.getGroupStrategy();
		m_run = run;
	}

	public String getGroupStrategyName() {
		return m_groupStrategyName != null ? m_groupStrategyName : null;
	}

	public String getGroupStrategyConfiguration() {
		return m_run.getGroupStrategyConfiguration() != null ? m_run.getGroupStrategyConfiguration() : null;
	}

	public Date getEndDate() {
		return m_run.getEndDate();
	}

	public Date getStartDate() {
		return m_run.getStartDate();
	}

	public boolean isEligible(Date date) {
		if (m_run.getDisabled() != null && m_run.getDisabled()) {
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

	public boolean isDisabled() {
		return m_run.isDisabled();
	}

	public void setDisabled(boolean disabled) {
		m_run.setDisabled(disabled);
	}

	public ABTestGroupStrategy getGroupStrategy() {
		return m_groupStrategy;
	}

	public void setGroupStrategy(ABTestGroupStrategy groupStrategy) {
		m_groupStrategy = groupStrategy;
	}
	
	public void setId(int id){
		m_id = id;
	}

	public int getId() {
		return m_id;
	}

	public String getName() {
   	return m_name;
   }

	public Run getRun() {
		return m_run;
	}

	@Override
   public int hashCode() {
	   final int prime = 31;
	   int result = 1;
	   result = prime * result + ((m_groupStrategyName == null) ? 0 : m_groupStrategyName.hashCode());
	   result = prime * result + m_id;
	   result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
	   result = prime * result + ((m_run == null) ? 0 : m_run.hashCode());
	   return result;
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
	   if (m_groupStrategyName == null) {
		   if (other.m_groupStrategyName != null)
			   return false;
	   } else if (!m_groupStrategyName.equals(other.m_groupStrategyName))
		   return false;
	   if (m_id != other.m_id)
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

	@Override
   public String toString() {
	   return "ABTestEntity [m_id=" + m_id + ", m_name=" + m_name + ", m_groupStrategyName=" + m_groupStrategyName
	         + ", m_run=" + m_run +"]";
   }

	
}