package com.dianping.tkv.util;

public class NumberKit {
	public static int bytes2Int(byte[] bytes) {
		return (bytes[0] & 0xff) << 24 | (bytes[1] & 0xff) << 16 | (bytes[2] & 0xff) << 8 | (bytes[3] & 0xff);
	}
	
	public static int bytes2Int(byte[] bytes, int offset) {
		return (bytes[offset++] & 0xff) << 24 | (bytes[offset++] & 0xff) << 16 | (bytes[offset++] & 0xff) << 8 | (bytes[offset++] & 0xff);
	}

	public static byte[] int2Bytes(int num) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (num >>> 24);
		bytes[1] = (byte) (num >>> 16);
		bytes[2] = (byte) (num >>> 8);
		bytes[3] = (byte) num;
		return bytes;
	}
}
