package com.dianping.cat.abtest.spi.internal.conditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserAgentParser {
	private static Map<String, UserAgent> m_agentCache = new HashMap<String, UserAgent>();

	private static int m_agentCacheSize = 1024;

	private static Pattern s_pattern = Pattern.compile("([^/\\s]*)(/([^\\s]*))?(\\s*\\[[a-zA-Z][a-zA-Z]\\])?"
	      + "\\s*(\\((([^()]|(\\([^()]*\\)))*)\\))?\\s*");

	/**
	 * Parses the incoming user agent string into useful data about the browser and its operating system.
	 * 
	 * @param userAgentString
	 *           the user agent header from the browser.
	 */
	public UserAgent parse(String userAgentString) {
		if (userAgentString == null) {
			return null;
		}

		UserAgent ua = null;
		synchronized (m_agentCache) {
			ua = m_agentCache.get(userAgentString);
		}

		if (ua == null) {
			Matcher matcher = s_pattern.matcher(userAgentString);

			List<UserAgent> uas = new ArrayList<UserAgent>();

			while (matcher.find()) {
				String nextBrowserName = matcher.group(1);
				String nextBrowserVersion = matcher.group(3);
				String nextBrowserComments = null;
				if (matcher.groupCount() >= 6) {
					nextBrowserComments = matcher.group(6);
				}

				ua = new UserAgent();

				ua.setBrowser(nextBrowserName);
				ua.setBrowserVersion(nextBrowserVersion);
				ua.setOs(extractOperatingSystem(nextBrowserComments));
				
				System.out.println(ua);
				uas.add(ua);
				break;
			}

			if (uas.size() > 0) {
			} else {
				throw new UserAgentParseException("Unable to parse user agent string: " + userAgentString);
			}

			synchronized (m_agentCache) {
				if (m_agentCache.size() >= m_agentCacheSize)
					m_agentCache.clear();
				m_agentCache.put(userAgentString, ua);
			}
		}

		return ua;
	}

	private String extractOperatingSystem(String comments) {

		if (comments == null) {
			return null;
		}

		String[] knownOS = new String[] { "win", "linux", "mac", "freebsd", "netbsd", "openbsd", "sunos", "amiga",
		      "beos", "irix", "os/2", "warp", "iphone" };
		List<String> osDetails = new ArrayList<String>();
		String[] parts = comments.split(";");
		for (String comment : parts) {
			String lowerComment = comment.toLowerCase().trim();
			for (String os : knownOS) {
				if (lowerComment.startsWith(os)) {
					osDetails.add(comment.trim());
				}
			}

		}
		switch (osDetails.size()) {
		case 0: {
			return null;
		}
		case 1: {
			return osDetails.get(0);
		}
		default: {
			return osDetails.get(0); // need to parse more stuff here
		}
		}

	}
}