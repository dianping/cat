/**
 * 
 */
package com.qbao.cat.plugin.db.sql;

import java.lang.reflect.Field;

import com.qbao.cat.plugin.constants.MyCatConstants;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Transaction;
import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.PreparedStatement;
import com.qbao.cat.plugin.DefaultPluginTemplate;

/**
 * 说明：
 * 1. 本plugin只针对用PreparedStatement执行sql的情况，pointcut表达式中如果埋到其它类中则无效
 * @author andersen
 *
 */
@Aspect
public abstract class MySQLPreparedStatementPluginTemplate extends DefaultPluginTemplate {
	
	private volatile static boolean effective = true;
	
	private volatile static Field originalSqlField = null;
	
	public MySQLPreparedStatementPluginTemplate() {
		try {
			originalSqlField = PreparedStatement.class.getDeclaredField("originalSql");
			if(isNotNull(originalSqlField)){
				originalSqlField.setAccessible(true);
			}else{
				effective = false;
			}
		} catch (Exception e) {
			effective = false;
		}
	}
	
	@Around(POINTCUT_NAME)
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		return super.doAround(pjp);
	}

	/* (non-Javadoc)
	 * @see cat.qbao.cat.plugin.PluginTemplate#scope()
	 */
	@Pointcut
	public void scope() {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see cat.qbao.cat.plugin.DefaultPluginTemplate#beginLog(org.aspectj.lang.ProceedingJoinPoint)
	 */
	protected Transaction beginLog(ProceedingJoinPoint pjp) {
		try {
			if (effective && pjp.getTarget() instanceof PreparedStatement){
				Transaction transaction = this.newTransaction(CatConstants.TYPE_SQL, (String)originalSqlField.get(pjp.getTarget()));
				PreparedStatement ps = (PreparedStatement)pjp.getTarget();
				if (ps.getConnection() instanceof ConnectionImpl){
					Cat.logEvent(MyCatConstants.TYPE_SQL_DATABASE, ((ConnectionImpl)((PreparedStatement)pjp.getTarget()).getConnection()).getURL());
				}			
				if ("true".equals(config.getProperty("plugin.mysql.ps.includefullsql"))){
					transaction.addData("FullSQL", ps.toString().split(":")[1]);
				}
				return transaction;
			}else{
				return null;
			}
		}catch (Exception e1) {
			return null;
		}
		
	}

	/* (non-Javadoc)
	 * @see cat.qbao.cat.plugin.DefaultPluginTemplate#endLog(com.dianping.cat.message.Transaction, java.lang.Object, java.lang.Object[])
	 */
	@Override
	protected void endLog(Transaction transaction, Object retVal, Object... params) {
		// TODO Auto-generated method stub

	}

}
