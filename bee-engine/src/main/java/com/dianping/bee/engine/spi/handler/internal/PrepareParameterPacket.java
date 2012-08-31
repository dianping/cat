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
public class PrepareParameterPacket extends MySQLPacket {

	private int m_fieldType;

	private int m_columnFlag;

	private byte m_decimal;

	private int m_length;

	public PrepareParameterPacket(int fieldType, int columnFlag, byte decimal, int length) {
		this.m_fieldType = fieldType;
		this.m_columnFlag = columnFlag;
		this.m_decimal = decimal;
		this.m_length = length;
	}

	@Override
	public ByteBuffer write(ByteBuffer buffer, FrontendConnection c) {
		int size = calcPacketSize();
		buffer = c.checkWriteBuffer(buffer, c.getPacketHeaderSize() + size);
		BufferUtil.writeUB3(buffer, size);
		buffer.put(packetId);
		BufferUtil.writeUB2(buffer, m_fieldType);
		BufferUtil.writeUB2(buffer, m_columnFlag);
		buffer.put(m_decimal);
		BufferUtil.writeUB4(buffer, m_length);
		return buffer;
	}

	@Override
	public int calcPacketSize() {
		return 9; // 2+2+1+4
	}

	@Override
	protected String getPacketInfo() {
		return "MySQL Prepare Parameter Packet";
	}

}
