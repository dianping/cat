/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-23
 * 
 * Copyright 2012 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.bee.engine.spi.internal;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.cobar.parser.ast.fragment.tableref.TableRefFactor;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableReference;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.alibaba.cobar.parser.visitor.EmptySQLASTVisitor;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class DefaultStatementVisitor extends EmptySQLASTVisitor {

	private Map<String, String> m_tableAlias = new HashMap<String, String>();

	public Map<String, String> getTableAlias() {
		return m_tableAlias;
	}

	@Override
	public void visit(DMLSelectStatement node) {
		TableReference tr = node.getTables();
		if (tr != null) {
			tr.accept(this);
		}
	}

	@Override
	public void visit(TableRefFactor node) {
		String alias = node.getAlias();
		String tableName = node.getTable().getIdTextUpUnescape();
		m_tableAlias.put(alias, tableName);
	}
}
