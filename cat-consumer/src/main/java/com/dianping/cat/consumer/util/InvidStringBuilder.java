package com.dianping.cat.consumer.util;

import sun.misc.BASE64Encoder;

public class InvidStringBuilder {

	public static final int s_lengthLimit = 256;

	public static String getValidString(String key) {
		String result = null;

		if (key.length() > s_lengthLimit) {
			result = key.substring(0, s_lengthLimit);
		} else {
			result = key;
		}

		StringBuilder sb = new StringBuilder(32);
		int length = result.length();
		boolean needBase64 = true;

		for (int i = 0; i < length; i++) {
			final char charAt = result.charAt(i);

			if (charAt > 126 || charAt < 32) {
				sb.append('.');
			} else {
				sb.append(charAt);
				needBase64 = false;
			}
		}
		if (needBase64) {
			return "Base64." + new BASE64Encoder().encodeBuffer(key.getBytes()).trim();
		} else {
			return sb.toString();
		}
	}
}
