package com.dianping.cat.report.task.browser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserAgentParser {

	private String m_userAgentString;

	private String m_browserName;

	private String m_browserVersion;

	private String m_browserOperatingSystem;

	private List<UserAgentDetail> m_parsedBrowsers = new ArrayList<UserAgentDetail>();

	private static Pattern m_pattern = Pattern.compile("([^/\\s]*)(/([^\\s]*))?(\\s*\\[[a-zA-Z][a-zA-Z]\\])?"
	      + "\\s*(\\((([^()]|(\\([^()]*\\)))*)\\))?\\s*");

	/**
	 * Parses the incoming user agent string into useful data about the browser and its operating system.
	 * 
	 * @param userAgentString
	 *           the user agent header from the browser.
	 */
	public UserAgentParser(String userAgentString) {
		this.m_userAgentString = userAgentString;
		Matcher matcher = m_pattern.matcher(userAgentString);

		while (matcher.find()) {
			String nextBrowserName = matcher.group(1);
			String nextBrowserVersion = matcher.group(3);
			String nextBrowserComments = null;

			if (matcher.groupCount() >= 6) {
				nextBrowserComments = matcher.group(6);
			}
			m_parsedBrowsers.add(new UserAgentDetail(nextBrowserName, nextBrowserVersion, nextBrowserComments));

		}

		if (m_parsedBrowsers.size() > 0) {
			processBrowserDetails();
		} else {
			throw new UserAgentParseException("Unable to parse user agent string: " + userAgentString);
		}

	}

	/**
	 * Iterates through all component browser details to try and find the canonical browser name and version.
	 * 
	 * @return a string array with browser name in element 0 and browser version in element 1. Null can be present in either or both.
	 */
	private String[] extractBrowserNameAndVersion() {

		String[] knownBrowsers = new String[] { "firefox", "netscape", "chrome", "safari", "camino", "mosaic", "opera",
		      "galeon" };

		for (UserAgentDetail nextBrowser : m_parsedBrowsers) {
			for (String nextKnown : knownBrowsers) {
				if (nextBrowser.getBrowserName().toLowerCase().startsWith(nextKnown)) {
					return new String[] { nextBrowser.getBrowserName(), nextBrowser.getBrowserVersion() };
				}
			}
		}
		UserAgentDetail firstAgent = m_parsedBrowsers.get(0);
		if (firstAgent.getBrowserName().toLowerCase().startsWith("mozilla")) {

			if (firstAgent.getBrowserComments() != null) {
				String[] comments = firstAgent.getBrowserComments().split(";");
				if (comments.length > 2 && comments[0].toLowerCase().startsWith("compatible")) {
					String realBrowserWithVersion = comments[1].trim();
					int firstSpace = realBrowserWithVersion.indexOf(' ');
					int firstSlash = realBrowserWithVersion.indexOf('/');
					if ((firstSlash > -1 && firstSpace > -1) || (firstSlash > -1 && firstSpace == -1)) {
						return new String[] { realBrowserWithVersion.substring(0, firstSlash),
						      realBrowserWithVersion.substring(firstSlash + 1) };
					} else if (firstSpace > -1) {
						return new String[] { realBrowserWithVersion.substring(0, firstSpace),
						      realBrowserWithVersion.substring(firstSpace + 1) };
					} else {
						return new String[] { realBrowserWithVersion, null };
					}
				}
			}

			// Looks like a *real* Mozilla :-)
			if (new Float(firstAgent.getBrowserVersion()) < 5.0) {
				return new String[] { "Netscape", firstAgent.getBrowserVersion() };
			} else {
				return new String[] { "Mozilla", firstAgent.getBrowserComments().split(";")[0].trim() };
			}
		} else {
			return new String[] { firstAgent.getBrowserName(), firstAgent.getBrowserVersion() };
		}

	}

	/**
	 * Extracts the operating system from the browser comments.
	 * 
	 * @param comments
	 *           the comment string after the browser version
	 * @return a string representing the operating system
	 */
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
			return osDetails.get(0);
		}
		}
	}

	public String getBrowserName() {
		return m_browserName;
	}

	public String getBrowserOperatingSystem() {
		return m_browserOperatingSystem;
	}

	public String getBrowserVersion() {
		return m_browserVersion;
	}

	public String getUserAgentString() {
		return m_userAgentString;
	}

	/**
	 * Wraps the process of extracting browser name, version, and operating sytem.
	 */
	private void processBrowserDetails() {
		String[] browserNameAndVersion = extractBrowserNameAndVersion();
		m_browserName = browserNameAndVersion[0];
		m_browserVersion = browserNameAndVersion[1];

		m_browserOperatingSystem = extractOperatingSystem(m_parsedBrowsers.get(0).getBrowserComments());
	}
}
