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
public class PrepareOKPacket extends MySQLPacket {

	private byte m_status = 0;

	private byte m_reserved = 0;

	private long m_statementId;

	private int m_columnSize;

	private int m_parameterSize;

	private int m_warningCount;

	public PrepareOKPacket(long statementId, int columnSize, int parameterSize) {
		this.m_statementId = statementId;
		this.m_columnSize = columnSize;
		this.m_parameterSize = parameterSize;
	}

	@Override
	public ByteBuffer write(ByteBuffer buffer, FrontendConnection c) {
		int size = calcPacketSize();
		buffer = c.checkWriteBuffer(buffer, c.getPacketHeaderSize() + size);
		BufferUtil.writeUB3(buffer, size);
		buffer.put(packetId);
		buffer.put(m_status);
		BufferUtil.writeUB4(buffer, m_statementId);
		BufferUtil.writeUB2(buffer, m_columnSize);
		BufferUtil.writeUB2(buffer, m_parameterSize);
		buffer.put(m_reserved);
		BufferUtil.writeUB2(buffer, m_warningCount);
		return buffer;
	}

	@Override
	public int calcPacketSize() {
		return 12;// 1+4+2+2+1+2
	}

	@Override
	protected String getPacketInfo() {
		return "MySQL Prepare OK Packet";
	}
}