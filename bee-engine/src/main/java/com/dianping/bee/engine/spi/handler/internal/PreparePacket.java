/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-31
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
package com.dianping.bee.engine.spi.handler.internal;

import java.nio.ByteBuffer;

import com.alibaba.cobar.net.FrontendConnection;
import com.alibaba.cobar.net.util.BufferUtil;
import com.alibaba.cobar.protocol.MySQLPacket;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class PreparePacket extends MySQLPacket {

	public static final byte FIELD_COUNT = 0x00;

	private byte m_fieldCount = FIELD_COUNT;

	private long m_statementId;

	private int m_columnSize;

	private int m_parameterSize;

	public PreparePacket(long statementId, int columnSize, int parameterSize) {
		this.m_statementId = statementId;
		this.m_columnSize = columnSize;
		this.m_parameterSize = parameterSize;
	}
	/**
	 * Bytes               Name
		-----               ----
		1                   field_count
		4                   statement_handler_id
		2                   columns
		2                   parameters

		field_count:         Always = 0, as with OK Packet.

		statement_handler_id: ID of statement handler.

		columns:             Number of columns in result set.

		parameters:          Number of parameters in query.
	 */
	@Override
	public ByteBuffer write(ByteBuffer buffer, FrontendConnection c) {
		int size = calcPacketSize();
		buffer = c.checkWriteBuffer(buffer, c.getPacketHeaderSize() + size);
		BufferUtil.writeUB3(buffer, size);
		buffer.put(packetId);
		buffer.put(m_fieldCount);
		BufferUtil.writeUB4(buffer, m_statementId);
		//FIXME: not compatible with document
		// BufferUtil.writeUB2(buffer, m_columnSize);
		BufferUtil.writeUB2(buffer, m_fieldCount);
		BufferUtil.writeUB2(buffer, m_parameterSize);
		return buffer;
	}

	@Override
	public int calcPacketSize() {
		return 9;// 1+4+2+2
	}

	@Override
	protected String getPacketInfo() {
		return "MySQL Prepare Packet";
	}
}