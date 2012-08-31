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

	private long statementId;

	private int columnSize;

	private int parameterSize;

	public PreparePacket(long statementId, int columnSize, int parameterSize) {
		this.statementId = statementId;
		this.columnSize = columnSize;
		this.parameterSize = parameterSize;
	}

	@Override
	public ByteBuffer write(ByteBuffer buffer, FrontendConnection c) {
		int size = calcPacketSize();
		buffer = c.checkWriteBuffer(buffer, c.getPacketHeaderSize() + size);
		BufferUtil.writeUB3(buffer, size);
		buffer.put(packetId);
		buffer.put((byte) 0);
		BufferUtil.writeUB4(buffer, statementId);
		BufferUtil.writeUB2(buffer, columnSize);
		BufferUtil.writeUB2(buffer, parameterSize);
		return buffer;
	}

	@Override
	public int calcPacketSize() {
		return 9;// 1+4+2+2
	}

	@Override
	protected String getPacketInfo() {
		return "MySQL Prepared Packet";
	}
}