package com.dianping.cat.notify.dao.ibatisimpl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.SqlMapClientCallback;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.ibatis.sqlmap.client.SqlMapExecutor;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

public class BaseDao extends SqlMapClientDaoSupport {
	public Object executeInsert(String statementName, Object parameterObject) {
		return this.insert(statementName, parameterObject);
	}

	public List<Object> executeQueryForList(String statementName,
			Object parameterObject) {
		return this.queryForList(statementName, parameterObject);
	}

	public Object executeQueryForObject(String statementName,
			Object parameterObject) {
		return this.queryForObject(statementName, parameterObject);
	}

	public int executeUpdate(String statementName, Object parameterObject) {
		return this.update(statementName, parameterObject);
	}

	public Object queryForObject(final String statementName,
			final Object parameterObject) {
		return this.getSqlMapClientTemplate().queryForObject(statementName,
				parameterObject);
	}

	@SuppressWarnings("unchecked")
	public List<Object> queryForList(String statementName) {
		return this.getSqlMapClientTemplate().queryForList(statementName);
	}

	@SuppressWarnings("unchecked")
	public List<Object> queryForList(String statementName,
			Map<String, Object> params, int start, int pageSize) {
		return this.getSqlMapClientTemplate().queryForList(statementName,
				params, start, pageSize);
	}

	@SuppressWarnings("unchecked")
	public List<Object> queryForList(final String statementName,
			final Object parameterObject) {
		return this.getSqlMapClientTemplate().queryForList(statementName,
				parameterObject);
	}

	public Object insert(String statementName) {
		return this.getSqlMapClientTemplate().insert(statementName);
	}

	public Object delete(String statementName, Object parameterObject) {
		return this.getSqlMapClientTemplate().delete(statementName,
				parameterObject);
	}

	public Object insert(String statementName, Object parameterObject) {
		return this.getSqlMapClientTemplate().insert(statementName,
				parameterObject);
	}

	public Object itInsert(String statementName, Object parameterObject)
			throws MySQLIntegrityConstraintViolationException {
		return this.getSqlMapClientTemplate().insert(statementName,
				parameterObject);
	}

	public int update(String statementName) {
		return this.update(statementName, null);
	}

	public int update(final String statementName, final Object parameterObject) {
		return this.getSqlMapClientTemplate().update(statementName,
				parameterObject);
	}

	public void banchInsert(final List<Object> list, final String statementName) {

		this.getSqlMapClientTemplate().execute(new SqlMapClientCallback() {

			public Object doInSqlMapClient(SqlMapExecutor executor)
					throws SQLException {
				executor.startBatch();
				for (Object obj : list) {
					executor.insert(statementName, obj);
				}
				executor.executeBatch();
				return null;
			}
		});
	}
}
