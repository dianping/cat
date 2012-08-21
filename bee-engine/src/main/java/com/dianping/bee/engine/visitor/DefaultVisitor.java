/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-15
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
package com.dianping.bee.engine.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cobar.parser.ast.ASTNode;
import com.alibaba.cobar.parser.ast.expression.BinaryOperatorExpression;
import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.PolyadicOperatorExpression;
import com.alibaba.cobar.parser.ast.expression.UnaryOperatorExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.BetweenAndExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionEqualsExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionIsExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionNullSafeEqualsExpression;
import com.alibaba.cobar.parser.ast.expression.comparison.InExpression;
import com.alibaba.cobar.parser.ast.expression.logical.LogicalAndExpression;
import com.alibaba.cobar.parser.ast.expression.logical.LogicalOrExpression;
import com.alibaba.cobar.parser.ast.expression.misc.InExpressionList;
import com.alibaba.cobar.parser.ast.expression.misc.QueryExpression;
import com.alibaba.cobar.parser.ast.expression.misc.UserExpression;
import com.alibaba.cobar.parser.ast.expression.primary.CaseWhenOperatorExpression;
import com.alibaba.cobar.parser.ast.expression.primary.DefaultValue;
import com.alibaba.cobar.parser.ast.expression.primary.ExistsPrimary;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.MatchExpression;
import com.alibaba.cobar.parser.ast.expression.primary.ParamMarker;
import com.alibaba.cobar.parser.ast.expression.primary.PlaceHolder;
import com.alibaba.cobar.parser.ast.expression.primary.RowExpression;
import com.alibaba.cobar.parser.ast.expression.primary.SysVarPrimary;
import com.alibaba.cobar.parser.ast.expression.primary.UsrDefVarPrimary;
import com.alibaba.cobar.parser.ast.expression.primary.function.FunctionExpression;
import com.alibaba.cobar.parser.ast.expression.primary.function.cast.Cast;
import com.alibaba.cobar.parser.ast.expression.primary.function.cast.Convert;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Avg;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Count;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.GroupConcat;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Max;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Min;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Sum;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Char;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Trim;
import com.alibaba.cobar.parser.ast.expression.primary.literal.IntervalPrimary;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralBitField;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralBoolean;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralHexadecimal;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralNull;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralNumber;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralString;
import com.alibaba.cobar.parser.ast.expression.string.LikeExpression;
import com.alibaba.cobar.parser.ast.expression.type.CollateExpression;
import com.alibaba.cobar.parser.ast.fragment.GroupBy;
import com.alibaba.cobar.parser.ast.fragment.Limit;
import com.alibaba.cobar.parser.ast.fragment.OrderBy;
import com.alibaba.cobar.parser.ast.fragment.SortOrder;
import com.alibaba.cobar.parser.ast.fragment.tableref.IndexHint;
import com.alibaba.cobar.parser.ast.fragment.tableref.InnerJoin;
import com.alibaba.cobar.parser.ast.fragment.tableref.NaturalJoin;
import com.alibaba.cobar.parser.ast.fragment.tableref.OuterJoin;
import com.alibaba.cobar.parser.ast.fragment.tableref.StraightJoin;
import com.alibaba.cobar.parser.ast.fragment.tableref.SubqueryFactor;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableRefFactor;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableReference;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableReferences;
import com.alibaba.cobar.parser.ast.stmt.dal.DALSetCharacterSetStatement;
import com.alibaba.cobar.parser.ast.stmt.dal.DALSetNamesStatement;
import com.alibaba.cobar.parser.ast.stmt.dal.DALSetStatement;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowAuthors;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowBinLogEvent;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowBinaryLog;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowCharaterSet;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowCollation;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowColumns;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowContributors;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowCreate;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowDatabases;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowEngine;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowEngines;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowErrors;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowEvents;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowFunctionCode;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowFunctionStatus;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowGrants;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowIndex;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowMasterStatus;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowOpenTables;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowPlugins;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowPrivileges;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowProcedureCode;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowProcedureStatus;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowProcesslist;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowProfile;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowProfiles;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowSlaveHosts;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowSlaveStatus;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowStatus;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowTableStatus;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowTables;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowTriggers;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowVariables;
import com.alibaba.cobar.parser.ast.stmt.dal.ShowWarnings;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLAlterTableStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLCreateIndexStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLCreateTableStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLDropIndexStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLDropTableStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLRenameTableStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DDLTruncateStatement;
import com.alibaba.cobar.parser.ast.stmt.ddl.DescTableStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLCallStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLDeleteStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLInsertStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLReplaceStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectUnionStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLUpdateStatement;
import com.alibaba.cobar.parser.ast.stmt.mts.MTSReleaseStatement;
import com.alibaba.cobar.parser.ast.stmt.mts.MTSRollbackStatement;
import com.alibaba.cobar.parser.ast.stmt.mts.MTSSavepointStatement;
import com.alibaba.cobar.parser.ast.stmt.mts.MTSSetTransactionStatement;
import com.alibaba.cobar.parser.util.Pair;
import com.alibaba.cobar.parser.visitor.SQLASTVisitor;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class DefaultVisitor implements SQLASTVisitor {
	private enum SELECT_STEP {
		SELECT, TABLE, WHERE, GROUP, HAVING, ORDER
	}

	private int idLevel = 2;

	private Map<String, Identifier> tables = new HashMap<String, Identifier>();

	private Map<Identifier, List<Identifier>> columns = new HashMap<Identifier, List<Identifier>>();

	private SELECT_STEP selectStep;

	private Expression whereExpression;

	public Map<Identifier, List<Identifier>> getColumns() {
		return this.columns;
	}

	public Map<String, Identifier> getTables() {
		return this.tables;
	}

	public Expression getWhere() {
		return this.whereExpression;
	}

	private void sortPairList(List<Pair<Expression, SortOrder>> list) {
		if (list == null || list.isEmpty())
			return;
		Expression[] exprs = new Expression[list.size()];
		int i = 0;
		for (Pair<Expression, SortOrder> p : list) {
			exprs[i] = p.getKey();
			++i;
		}
		visitChild(2, exprs);
	}

	@Override
	public void visit(Avg node) {
		visitChild(2, node.getArguments());
	}

	@Override
	public void visit(BetweenAndExpression node) {
		Expression fst = node.getFirst();
		Expression snd = node.getSecond();
		Expression trd = node.getThird();

		visitChild(2, fst, snd, trd);
	}

	@Override
	public void visit(BinaryOperatorExpression node) {
		Expression left = node.getLeftOprand();
		Expression right = node.getRightOprand();
		visitChild(2, left, right);
	}

	@Override
	public void visit(CaseWhenOperatorExpression node) {
		visitChild(2, node.getComparee(), node.getElseResult());
		List<Pair<Expression, Expression>> whenPairList = node.getWhenList();
		if (whenPairList == null || whenPairList.isEmpty())
			return;
		List<Expression> list = new ArrayList<Expression>(whenPairList.size() * 2);
		for (Pair<Expression, Expression> pair : whenPairList) {
			if (pair == null)
				continue;
			list.add(pair.getKey());
			list.add(pair.getValue());
		}
		visitChild(2, list);
	}

	@Override
	public void visit(Cast node) {
		visitChild(2, node.getArguments());
		visitChild(2, node.getTypeInfo1(), node.getTypeInfo2());
	}

	@Override
	public void visit(Char node) {
		visitChild(2, node.getArguments());
	}

	@Override
	public void visit(CollateExpression node) {
		visitChild(2, node.getString());
	}

	@Override
	public void visit(ComparisionEqualsExpression node) {
		Expression left = node.getLeftOprand();
		Expression right = node.getRightOprand();
		visitChild(2, left, right);
	}

	@Override
	public void visit(ComparisionIsExpression node) {
		Expression operand = node.getOperand();
		visitChild(2, operand);
	}

	@Override
	public void visit(ComparisionNullSafeEqualsExpression node) {
		Expression left = node.getLeftOprand();
		Expression right = node.getRightOprand();
		visitChild(2, left, right);
	}

	@Override
	public void visit(Convert node) {
		visitChild(2, node.getArguments());
	}

	@Override
	public void visit(Count node) {
		visitChild(2, node.getArguments());
	}

	@Override
	public void visit(DALSetCharacterSetStatement node) {
	}

	@Override
	public void visit(DALSetNamesStatement node) {
	}

	@Override
	public void visit(DALSetStatement node) {
	}

	@Override
	public void visit(DDLAlterTableStatement node) {
	}

	@Override
	public void visit(DDLCreateIndexStatement node) {
	}

	@Override
	public void visit(DDLCreateTableStatement node) {
	}

	@Override
	public void visit(DDLDropIndexStatement node) {
	}

	@Override
	public void visit(DDLDropTableStatement node) {
	}

	@Override
	public void visit(DDLRenameTableStatement node) {
	}

	@Override
	public void visit(DDLTruncateStatement node) {
	}

	@Override
	public void visit(DefaultValue node) {
	}

	@Override
	public void visit(DescTableStatement node) {
	}

	@Override
	public void visit(DMLCallStatement node) {
	}

	@Override
	public void visit(DMLDeleteStatement node) {
	}

	@Override
	public void visit(DMLInsertStatement node) {
	}

	@Override
	public void visit(DMLReplaceStatement node) {
	}

	@Override
	public void visit(DMLSelectStatement node) {
		selectStep = SELECT_STEP.TABLE;
		TableReference tr = node.getTables();
		visitChild(1, tr);

		selectStep = SELECT_STEP.SELECT;
		List<Expression> exprList = node.getSelectExprListWithoutAlias();
		visitChild(2, exprList);

		selectStep = SELECT_STEP.WHERE;
		Expression where = node.getWhere();
		
		visitChild(2, where);
		this.whereExpression = where;

		selectStep = SELECT_STEP.GROUP;
		GroupBy group = node.getGroup();
		visitChild(2, group);

		selectStep = SELECT_STEP.HAVING;
		Expression having = node.getHaving();
		visitChild(2, having);

		selectStep = SELECT_STEP.ORDER;
		OrderBy order = node.getOrder();
		visitChild(2, order);

	}

	@Override
	public void visit(DMLSelectUnionStatement node) {
		visitChild(2, node.getOrderBy());
		visitChild(2, node.getSelectStmtList());
	}

	@Override
	public void visit(DMLUpdateStatement node) {
	}

	@Override
	public void visit(ExistsPrimary node) {
		visitChild(2, node.getSubquery());
	}

	@Override
	public void visit(FunctionExpression node) {
		visitChild(2, node.getArguments());
	}

	@Override
	public void visit(GroupBy node) {
		sortPairList(node.getOrderByList());
	}

	@Override
	public void visit(GroupConcat node) {
		visitChild(2, node.getArguments());
	}

	@Override
	public void visit(Identifier node) {
		switch (selectStep) {
		case SELECT:
			Identifier table = node.getParent();
			if (!columns.containsKey(table)) {
				List<Identifier> column = new ArrayList<Identifier>();
				columns.put(table, column);
			}
			columns.get(table).add(node);
			break;
		case TABLE:
			tables.put(node.getIdTextUpUnescape(), node);
			break;
		case WHERE:
			break;
		case GROUP:
			break;
		case HAVING:
			break;
		case ORDER:
			break;
		default:
			System.out.println(node);
		}
	}

	@Override
	public void visit(IndexHint node) {
	}

	@Override
	public void visit(InExpression node) {
		Expression left = node.getLeftOprand();
		Expression right = node.getRightOprand();
		visitChild(2, left, right);
	}

	@Override
	public void visit(InExpressionList node) {
		visitChild(2, node.getList());
	}

	@Override
	public void visit(InnerJoin node) {
		TableReference tr1 = node.getLeftTableRef();
		TableReference tr2 = node.getRightTableRef();
		Expression on = node.getOnCond();
		visitChild(1, tr1, tr2);
		visitChild(2, on);
	}

	@Override
	public void visit(IntervalPrimary node) {
		visitChild(2, node.getQuantity());
	}

	@Override
	public void visit(LikeExpression node) {
		visitChild(2, node.getFirst(), node.getSecond(), node.getThird());
	}

	@Override
	public void visit(Limit node) {
	}

	@Override
	public void visit(LiteralBitField node) {
	}

	@Override
	public void visit(LiteralBoolean node) {
	}

	@Override
	public void visit(LiteralHexadecimal node) {
	}

	@Override
	public void visit(LiteralNull node) {
	}

	@Override
	public void visit(LiteralNumber node) {
	}

	@Override
	public void visit(LiteralString node) {
	}

	@Override
	public void visit(LogicalAndExpression node) {
		for (int i = 0, len = node.getArity(); i < len; ++i) {
			Expression oprand = node.getOperand(i);
			visitChild(2, oprand);
		}
	}

	@Override
	public void visit(LogicalOrExpression node) {
		for (int i = 0, len = node.getArity(); i < len; ++i) {
			Expression oprand = node.getOperand(i);
			visitChild(2, oprand);
		}
	}

	@Override
	public void visit(MatchExpression node) {
		visitChild(2, node.getColumns());
		visitChild(2, node.getPattern());
	}

	@Override
	public void visit(Max node) {
		visitChild(2, node.getArguments());
	}

	@Override
	public void visit(Min node) {
		visitChild(2, node.getArguments());
	}

	@Override
	public void visit(MTSReleaseStatement node) {
	}

	@Override
	public void visit(MTSRollbackStatement node) {
	}

	@Override
	public void visit(MTSSavepointStatement node) {
	}

	@Override
	public void visit(MTSSetTransactionStatement node) {
	}

	@Override
	public void visit(NaturalJoin node) {
		TableReference tr1 = node.getLeftTableRef();
		TableReference tr2 = node.getRightTableRef();
		visitChild(1, tr1, tr2);
	}

	@Override
	public void visit(OrderBy node) {
		sortPairList(node.getOrderByList());
	}

	@Override
	public void visit(OuterJoin node) {
		TableReference tr1 = node.getLeftTableRef();
		TableReference tr2 = node.getRightTableRef();
		Expression on = node.getOnCond();
		visitChild(1, tr1, tr2);
		visitChild(2, on);
	}

	@Override
	public void visit(ParamMarker node) {
	}

	@Override
	public void visit(PlaceHolder node) {
	}

	@Override
	public void visit(PolyadicOperatorExpression node) {
	}

	@Override
	public void visit(RowExpression node) {
		visitChild(2, node.getRowExprList());
	}

	@Override
	public void visit(ShowAuthors node) {
	}

	@Override
	public void visit(ShowBinaryLog node) {
	}

	@Override
	public void visit(ShowBinLogEvent node) {
	}

	@Override
	public void visit(ShowCharaterSet node) {
	}

	@Override
	public void visit(ShowCollation node) {
	}

	@Override
	public void visit(ShowColumns node) {
	}

	@Override
	public void visit(ShowContributors node) {
	}

	@Override
	public void visit(ShowCreate node) {
	}

	@Override
	public void visit(ShowDatabases node) {
	}

	@Override
	public void visit(ShowEngine node) {
	}

	@Override
	public void visit(ShowEngines node) {
	}

	@Override
	public void visit(ShowErrors node) {
	}

	@Override
	public void visit(ShowEvents node) {
	}

	@Override
	public void visit(ShowFunctionCode node) {
	}

	@Override
	public void visit(ShowFunctionStatus node) {
	}

	@Override
	public void visit(ShowGrants node) {
	}

	@Override
	public void visit(ShowIndex node) {
	}

	@Override
	public void visit(ShowMasterStatus node) {
	}

	@Override
	public void visit(ShowOpenTables node) {
	}

	@Override
	public void visit(ShowPlugins node) {
	}

	@Override
	public void visit(ShowPrivileges node) {
	}

	@Override
	public void visit(ShowProcedureCode node) {
	}

	@Override
	public void visit(ShowProcedureStatus node) {
	}

	@Override
	public void visit(ShowProcesslist node) {
	}

	@Override
	public void visit(ShowProfile node) {
	}

	@Override
	public void visit(ShowProfiles node) {
	}

	@Override
	public void visit(ShowSlaveHosts node) {
	}

	@Override
	public void visit(ShowSlaveStatus node) {
	}

	@Override
	public void visit(ShowStatus node) {
	}

	@Override
	public void visit(ShowTables node) {
	}

	@Override
	public void visit(ShowTableStatus node) {
	}

	@Override
	public void visit(ShowTriggers node) {
	}

	@Override
	public void visit(ShowVariables node) {
	}

	@Override
	public void visit(ShowWarnings node) {
	}

	@Override
	public void visit(StraightJoin node) {
		TableReference tr1 = node.getLeftTableRef();
		TableReference tr2 = node.getRightTableRef();
		Expression on = node.getOnCond();
		visitChild(1, tr1, tr2);
		visitChild(2, on);
	}

	@Override
	public void visit(SubqueryFactor node) {
		QueryExpression query = node.getSubquery();
		visitChild(2, query);
	}

	@Override
	public void visit(Sum node) {
		visitChild(2, node.getArguments());
	}

	@Override
	public void visit(SysVarPrimary node) {
	}

	@Override
	public void visit(TableReferences node) {
		List<TableReference> list = node.getTableReferenceList();
		visitChild(1, list);
	}

	public void visit(TableRefFactor node) {
		visitChild(1, node.getTable());
	}

	@Override
	public void visit(Trim node) {
		visitChild(2, node.getArguments());
	}

	@Override
	public void visit(UnaryOperatorExpression node) {
		visitChild(2, node.getOperand());
	}

	@Override
	public void visit(UserExpression node) {
	}

	@Override
	public void visit(UsrDefVarPrimary node) {
	}

	private void visitChild(int idLevel, ASTNode... nodes) {
		if (nodes == null || nodes.length <= 0)
			return;
		int oldLevel = this.idLevel;
		this.idLevel = idLevel;
		try {
			for (ASTNode node : nodes) {
				if (node != null) {
					node.accept(this);
				}
			}
		} finally {
			this.idLevel = oldLevel;
		}
	}

	private void visitChild(int idLevel, List<? extends ASTNode> nodes) {
		if (nodes == null || nodes.isEmpty())
			return;
		int oldLevel = this.idLevel;
		this.idLevel = idLevel;
		try {
			for (ASTNode node : nodes) {
				if (node != null) {
					node.accept(this);
				}
			}
		} finally {
			this.idLevel = oldLevel;
		}
	}
}
