/**
 * Project: bee-engine
 * 
 * File Created at 2012-9-6
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
import java.util.ArrayList;
import java.util.List;

import com.alibaba.cobar.net.FrontendConnection;
import com.alibaba.cobar.net.util.BufferUtil;
import com.alibaba.cobar.protocol.MySQLPacket;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class BinaryRowDataPacket extends MySQLPacket {

	private final byte header = 0;

	private final int fieldCount;

	private final List<byte[]> fieldValues;

	private final byte[] fieldsLength;

	private byte[] bitMap;

	public BinaryRowDataPacket(int fieldCount) {
		this.fieldCount = fieldCount;
		this.fieldValues = new ArrayList<byte[]>(fieldCount);
		this.fieldsLength = new byte[fieldCount];
		this.bitMap = new byte[(fieldCount + 7 + 2) / 8];
	}

	public void add(byte[] value, byte fieldLength) {
		fieldValues.add(value);
		this.fieldsLength[fieldValues.size() - 1] = fieldLength;
	}

	@Override
	public ByteBuffer write(ByteBuffer bb, FrontendConnection c) {
		bb = c.checkWriteBuffer(bb, c.getPacketHeaderSize());
		BufferUtil.writeUB3(bb, calcPacketSize());
		bb.put(packetId);
		bb.put(header);
		bb.put(bitMap);
		for (int i = 0; i < fieldCount; i++) {
			byte[] fv = fieldValues.get(i);
			bb = c.checkWriteBuffer(bb, BufferUtil.getLength(fv.length));
			if (fieldsLength[i] < 0) {
				BufferUtil.writeLength(bb, fieldValues.get(i).length);
			}
			bb = c.writeToBuffer(fv, bb);
		}
		return bb;
	}

	@Override
	public int calcPacketSize() {
		int size = 1 + bitMap.length;
		for (int i = 0; i < fieldCount; i++) {
			if (fieldsLength[i] > 0) {
				size += fieldsLength[i];
			} else {
				size += fieldValues.get(i).length + 1;
			}
		}
		return size;
	}

	@Override
	protected String getPacketInfo() {
		return "MySQL BinaryRowData Packet";
	}

}
