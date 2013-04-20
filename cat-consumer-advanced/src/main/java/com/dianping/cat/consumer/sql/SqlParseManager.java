package com.dianping.cat.consumer.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.consumer.advanced.dal.Sqltable;
import com.dainping.cat.consumer.advanced.dal.SqltableDao;
import com.dainping.cat.consumer.advanced.dal.SqltableEntity;
import com.dianping.cat.Cat;

public class SqlParseManager {
	@Inject
	private SqltableDao m_sqltableDao;

	private Set<String> m_domains = new HashSet<String>();

	private Map<String, String> m_sqltables = new HashMap<String, String>();

	public String getTableNames(String sqlName, String sqlStatement, String domain) {
		if (!m_domains.contains(domain)) {
			loadAllFromDatabase(domain);
		}

		String table = m_sqltables.get(sqlName);
		if (table == null) {
			table = parseSql(sqlStatement);

			insert(sqlName, table, sqlStatement, domain);
			m_sqltables.put(sqlName, table);
		}
		return table;
	}

	private void insert(String sqlName, String tableName, String sqlStatement, String domain) {
		Sqltable sqltable = m_sqltableDao.createLocal();

		sqltable.setDomain(domain);
		sqltable.setSqlName(sqlName);
		sqltable.setSqlStatement(sqlStatement);
		sqltable.setTableName(tableName);

		try {
			m_sqltableDao.insert(sqltable);
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	private synchronized void loadAllFromDatabase(String domain) {
		if (m_domains.contains(domain)) {
			return;
		}
		try {
			List<Sqltable> sqltables = m_sqltableDao.findAllByDomain(domain, SqltableEntity.READSET_FULL);

			for (Sqltable sqltable : sqltables) {
				m_sqltables.put(sqltable.getSqlName(), sqltable.getTableName());
			}

			m_domains.add(domain);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private String parseSql(String sqlStatement) {
		List<String> tables = null;
		try {
			tables = SqlParsers.forTable().parse(sqlStatement);
		} catch (Exception e) {
			Cat.logError(e);
			Cat.getProducer().logEvent("Parse", "SQL", "Error", sqlStatement);
		}
		String result = "";
		boolean first = true;

		if (tables != null && tables.size() > 0) {
			for (String table : tables) {
				if (first) {
					result = table;
				} else {
					result = result + ":" + table;
				}
			}
		} else {
			result = "UnKnownTable";
		}
		return result;
	}
}
