package com.dianping.cat.consumer.browser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserAgentParser {
	 private String userAgentString;
    private String browserName;
    private String browserVersion;
    private String browserOperatingSystem;
    private List<UserAgentDetails> parsedBrowsers = new ArrayList<UserAgentDetails>();

    private static Pattern pattern = Pattern.compile(
            "([^/\\s]*)(/([^\\s]*))?(\\s*\\[[a-zA-Z][a-zA-Z]\\])?" +
            "\\s*(\\((([^()]|(\\([^()]*\\)))*)\\))?\\s*");

    /**
     * Parses the incoming user agent string into useful data about
     * the browser and its operating system.
     * @param userAgentString the user agent header from the browser.
     */
    public UserAgentParser(String userAgentString) {
        this.userAgentString = userAgentString;
        Matcher matcher = pattern.matcher(userAgentString);

        while (matcher.find()) {
            String nextBrowserName = matcher.group(1);
            String nextBrowserVersion = matcher.group(3);
            String nextBrowserComments = null;
            
            if (matcher.groupCount() >= 6) {
                nextBrowserComments = matcher.group(6);
            }
            parsedBrowsers.add(new UserAgentDetails(nextBrowserName,
                    nextBrowserVersion, nextBrowserComments));
            
        }
        
        if (parsedBrowsers.size() > 0) {
            processBrowserDetails();
        } else {
            throw new UserAgentParseException("Unable to parse user agent string: " + userAgentString);
        }

    }

    /**
     * Iterates through all component browser details to try and find the
     * canonical browser name and version.
     *
     * @return a string array with browser name in element 0 and browser
     * version in element 1. Null can be present in either or both.
     */
    private String[] extractBrowserNameAndVersion() {

        String[] knownBrowsers = new String[] {
            "firefox", "netscape", "chrome", "safari", "camino", "mosaic", "opera",
            "galeon"
        };

        for(UserAgentDetails nextBrowser : parsedBrowsers) {
            for (String nextKnown : knownBrowsers) {
                if (nextBrowser.getBrowserName().toLowerCase().startsWith(nextKnown)) {
                    return new String[] { nextBrowser.getBrowserName(), nextBrowser.getBrowserVersion() };
                }
            }
        }
        UserAgentDetails firstAgent = parsedBrowsers.get(0);
        if (firstAgent.getBrowserName().toLowerCase().startsWith("mozilla")) {

            if (firstAgent.getBrowserComments() != null) {
                String[] comments = firstAgent.getBrowserComments().split(";");
                if (comments.length > 2 && comments[0].toLowerCase().startsWith("compatible")) {
                    String realBrowserWithVersion = comments[1].trim();
                    int firstSpace = realBrowserWithVersion.indexOf(' ');
                    int firstSlash = realBrowserWithVersion.indexOf('/');
                    if ((firstSlash > -1 && firstSpace > -1) ||
                            (firstSlash > -1 && firstSpace == -1)) {
                        // we have slash and space, or just a slash,
                        // so let's choose slash for the split
                        return new String[] {
                            realBrowserWithVersion.substring(0, firstSlash),
                            realBrowserWithVersion.substring(firstSlash+1)
                        };
                    }
                    else if (firstSpace > -1) {
                        return new String[] {
                           realBrowserWithVersion.substring(0, firstSpace),
                           realBrowserWithVersion.substring(firstSpace+1)
                        };
                    }
                    else { // out of ideas for version, or no version supplied
                        return new String[] { realBrowserWithVersion, null };
                    }
                }
            }

            // Looks like a *real* Mozilla :-)
            if (new Float(firstAgent.getBrowserVersion()) < 5.0) {
                return new String[] { "Netscape", firstAgent.getBrowserVersion() };
            } else {
                // TODO: get version from comment string
                return new String[] { "Mozilla",
                    firstAgent.getBrowserComments().split(";")[0].trim() };
            }
        } else {
            return new String[] {
                firstAgent.getBrowserName(), firstAgent.getBrowserVersion()
            };
        }
        
    }

    /**
     * Extracts the operating system from the browser comments.
     *
     * @param comments the comment string after the browser version
     * @return a string representing the operating system
     */
    private String extractOperatingSystem(String comments) {

        if (comments == null) {
            return null;
        }

        String[] knownOS = new String[] { "win", "linux", "mac", "freebsd", "netbsd",
                "openbsd", "sunos", "amiga", "beos", "irix", "os/2", "warp", "iphone" };
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
            case 0: { return null; }
            case 1: { return osDetails.get(0); }
            default: {
                return osDetails.get(0); // need to parse more stuff here
            }
        }
    }

    public String getBrowserName() {
        return browserName;
    }

    public String getBrowserOperatingSystem() {
        return browserOperatingSystem;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public String getUserAgentString() {
   	return userAgentString;
    }

	/**
     * Wraps the process of extracting browser name, version, and
     * operating sytem.
     */
    private void processBrowserDetails() {
        String[] browserNameAndVersion = extractBrowserNameAndVersion();
        browserName = browserNameAndVersion[0];
        browserVersion = browserNameAndVersion[1];
       
        browserOperatingSystem = extractOperatingSystem(parsedBrowsers.get(0).getBrowserComments());
    }
}
