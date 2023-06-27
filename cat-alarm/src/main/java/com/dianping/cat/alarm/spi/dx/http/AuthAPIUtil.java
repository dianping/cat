package com.dianping.cat.alarm.spi.dx.http;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AuthAPIUtil {
	private static DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	static {
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public static String getDateString(Date date) {
		return df.format(date);
	}

	public static Date toDate(String date) throws ParseException {
		synchronized (df) {
			return df.parse(date);
		}
	}

	public static String getAuthorization(String uri, String method, String date, String clientId, String secret) {
		String stringToSign = method + " " + uri + "\n" + date;

		String signature = getSignature(stringToSign, secret);

		String authorization = "MWS" + " " + clientId + ":" + signature;

		return authorization;
	}

	public static String getSignature(String data, String secret) {
		String result;
		try {
			SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);

			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);

			byte[] rawHmac = mac.doFinal(data.getBytes());

			result = Base64.encodeBase64String(rawHmac);
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate HMAC : ", e);
		}
		return result;
	}

	private static String[] getSignHeaders(String url, String appKey, String token, String method) {
		if (token == null || token.isEmpty()) {
			return new String[0];
		}

		if (url == null || !url.contains("//")) {
			return new String[0];
		}

		try {
			URL uri1 = new URL(url);

			String uri = uri1.getPath();

			String date = getDateString(new Date());
			String check = getSignature(String.format("%s %s\n%s", method, uri, date), token);

			return new String[] { "Authorization", String.format("MWS %s:%s", appKey, check), "Date", date };
		} catch (MalformedURLException e) {
			return new String[0];
		}
	}

	public static String[] getGetSignHeaders(String url, String appKey, String token) {
		return getSignHeaders(url, appKey, token, "GET");
	}

	public static String[] getPostSignHeaders(String url, String appKey, String token) {
		return getSignHeaders(url, appKey, token, "POST");
	}

	public static String[] getPutSignHeaders(String url, String appKey, String token) {
		return getSignHeaders(url, appKey, token, "PUT");
	}

}
