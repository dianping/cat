/**
 * Project: bee-engine
 * 
 * File Created at 2012-9-4
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
package com.dianping.bee.engine.spi;

import com.dianping.bee.engine.spi.meta.ColumnMeta;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public interface PreparedStatement extends Statement {
	public long getStatementId();

	public void setStatementId(long statementId);

	public ColumnMeta getParameterMeta(int paramIndex);

	public int getParameterSize();

	public void setParameter(int index, Object param);
}
