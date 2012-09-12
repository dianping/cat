package com.dianping.dog.alarm.rule;

import java.util.List;

import com.dianping.dog.alarm.entity.RuleEntity;
import com.site.dal.jdbc.AbstractDao;
import com.site.dal.jdbc.DalException;
import com.site.dal.jdbc.Updateset;

public class RuleEntityDAO extends AbstractDao {

	public RuleEntity createLocal() {
		RuleEntity proto = new RuleEntity();
		return proto;
	}
	
	public List<RuleEntity> findAll(){
		return null;
	}
	

   public int deleteByPK(RuleEntity proto) throws DalException {
     return 0;
   }
   
   public int updateRuleEntity(RuleEntity proto, Updateset<RuleEntity> updateset){
   	return 0;
   }
   
   public int insertRuleEntity(RuleEntity ruleEntity){
   	return 0;
   }

	@Override
	protected Class<?>[] getEntityClasses() {
		return new Class<?>[] { RuleEntity.class };
	}

}
