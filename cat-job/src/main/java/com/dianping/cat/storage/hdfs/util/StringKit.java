package com.dianping.cat.storage.hdfs.util;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * 字符串处理
 * 
 * @author cilai
 * @since 1.0, 2010-4-1 下午07:02:52
 */

public class StringKit {
	/** The Constant QUOTE. */
	public static final String QUOTE = "\\\\";

	/** The Constant BACK_SLASH. */
	public static final String BACK_SLASH = "\\\"";

	/**
	 * 将字符串的首字符传唤为大写
	 * 
	 * @param str
	 * @return
	 */
	public static String capitalize(String str) {
		if (str == null || str == "")
			return str;
		char[] chars = str.toCharArray();
		char c = chars[0];
		if (c >= 97 && c <= 122)
			chars[0] = (char) (c - 32);
		return new String(chars);
	}

	public static void writeEscapeJson(Writer sb, String source)
			throws IOException {
		char[] chars = source.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			switch (c) {
			case '"':
				sb.append(BACK_SLASH);
				break;
			case '\\':
				sb.append(QUOTE);
				break;
			default:
				sb.append(c);
			}
		}
	}

	/** The Constant QUOT. */
	public static final String QUOT = "&quot;";

	/** The Constant AMP. */
	public static final String AMP = "&amp;";

	/** The Constant APOS. */
	public static final String APOS = "&apos;";

	/** The Constant GT. */
	public static final String GT = "&gt;";

	/** The Constant LT. */
	public static final String LT = "&lt;";

	/**
	 * xml字符转义包括(<,>,',&,")五个字符.
	 * 
	 * @param string
	 *            所需转义的字符串
	 * 
	 * @return 转义后的字符串
	 * @throws IOException
	 */
	public static void writeEscapeXml(Writer writer, String string)
			throws IOException {
		char[] chars = string.trim().toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			switch (c) {
			case '<':
				writer.append(LT);
				break;
			case '>':
				writer.append(GT);
				break;
			case '\'':
				writer.append(APOS);
				break;
			case '&':
				writer.append(AMP);
				break;
			case '\"':
				writer.append(QUOT);
				break;
			default:
				if ((c == 0x9) || (c == 0xA) || (c == 0xD)
						|| ((c >= 0x20) && (c <= 0xD7FF))
						|| ((c >= 0xE000) && (c <= 0xFFFD))
						|| ((c >= 0x10000) && (c <= 0x10FFFF)))
					writer.append(c);
			}
		}
	}

	/**
	 * 过滤不可见字符
	 * 
	 * @author liupo
	 * @param input
	 * @return
	 */
	public static String stripNonValidXMLCharacters(String input) {
		if (input == null || ("".equals(input)))
			return "";
		StringBuilder out = new StringBuilder();
		char current;
		for (int i = 0; i < input.length(); i++) {
			current = input.charAt(i);
			if ((current == 0x9) || (current == 0xA) || (current == 0xD)
					|| ((current >= 0x20) && (current <= 0xD7FF))
					|| ((current >= 0xE000) && (current <= 0xFFFD))
					|| ((current >= 0x10000) && (current <= 0x10FFFF)))
				out.append(current);
		}
		return out.toString();
	}

	/**
	 * <p>
	 * Splits the provided text into an array, separator specified. This is an
	 * alternative to using StringTokenizer.
	 * </p>
	 * 
	 * <p>
	 * The separator is not included in the returned String array. Adjacent
	 * separators are treated as one separator. For more control over the split
	 * use the StrTokenizer class.
	 * </p>
	 * 
	 * <p>
	 * A <code>null</code> input String returns <code>null</code>.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.split(null, *)         = null
	 * StringUtils.split("", *)           = []
	 * StringUtils.split("a.b.c", '.')    = ["a", "b", "c"]
	 * StringUtils.split("a..b.c", '.')   = ["a", "b", "c"]
	 * StringUtils.split("a:b:c", '.')    = ["a:b:c"]
	 * StringUtils.split("a b c", ' ')    = ["a", "b", "c"]
	 * </pre>
	 * 
	 * @param str
	 *            the String to parse, may be null
	 * @param separatorChar
	 *            the character used as the delimiter
	 * @return an array of parsed Strings, <code>null</code> if null String
	 *         input
	 * @since 2.0
	 */
	public static String[] split(String str, char separatorChar) {
		return splitWorker(str, separatorChar, false);
	}
	

    /**
     * <p>Splits the provided text into an array, separator specified,
     * preserving all tokens, including empty tokens created by adjacent
     * separators. This is an alternative to using StringTokenizer.</p>
     *
     * <p>The separator is not included in the returned String array.
     * Adjacent separators are treated as separators for empty tokens.
     * For more control over the split use the StrTokenizer class.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.</p>
     *
     * <pre>
     * StringUtils.splitPreserveAllTokens(null, *)         = null
     * StringUtils.splitPreserveAllTokens("", *)           = []
     * StringUtils.splitPreserveAllTokens("a.b.c", '.')    = ["a", "b", "c"]
     * StringUtils.splitPreserveAllTokens("a..b.c", '.')   = ["a", "", "b", "c"]
     * StringUtils.splitPreserveAllTokens("a:b:c", '.')    = ["a:b:c"]
     * StringUtils.splitPreserveAllTokens("a\tb\nc", null) = ["a", "b", "c"]
     * StringUtils.splitPreserveAllTokens("a b c", ' ')    = ["a", "b", "c"]
     * StringUtils.splitPreserveAllTokens("a b c ", ' ')   = ["a", "b", "c", ""]
     * StringUtils.splitPreserveAllTokens("a b c  ", ' ')   = ["a", "b", "c", "", ""]
     * StringUtils.splitPreserveAllTokens(" a b c", ' ')   = ["", a", "b", "c"]
     * StringUtils.splitPreserveAllTokens("  a b c", ' ')  = ["", "", a", "b", "c"]
     * StringUtils.splitPreserveAllTokens(" a b c ", ' ')  = ["", a", "b", "c", ""]
     * </pre>
     *
     * @param str  the String to parse, may be <code>null</code>
     * @param separatorChar  the character used as the delimiter,
     *  <code>null</code> splits on whitespace
     * @return an array of parsed Strings, <code>null</code> if null String input
     * @since 2.1
     */
    public static String[] splitPreserveAllTokens(String str, char separatorChar) {
        return splitWorker(str, separatorChar, true);
    }

	/**
	 * Performs the logic for the <code>split</code> and
	 * <code>splitPreserveAllTokens</code> methods that do not return a maximum
	 * array length.
	 * 
	 * @param str
	 *            the String to parse, may be <code>null</code>
	 * @param separatorChar
	 *            the separate character
	 * @param preserveAllTokens
	 *            if <code>true</code>, adjacent separators are treated as empty
	 *            token separators; if <code>false</code>, adjacent separators
	 *            are treated as one separator.
	 * @return an array of parsed Strings, <code>null</code> if null String
	 *         input
	 */
	private static String[] splitWorker(String str, char separatorChar,
			boolean preserveAllTokens) {
		// Performance tuned for 2.0 (JDK1.4)

		if (str == null) {
			return null;
		}
		int len = str.length();
		if (len == 0) {
			return ArrayKit.EMPTY_STRING_ARRAY;
		}
		List<String> list = new ArrayList<String>();
		int i = 0, start = 0;
		boolean match = false;
		boolean lastMatch = false;
		while (i < len) {
			if (str.charAt(i) == separatorChar) {
				if (match || preserveAllTokens) {
					list.add(str.substring(start, i));
					match = false;
					lastMatch = true;
				}
				start = ++i;
				continue;
			}
			lastMatch = false;
			match = true;
			i++;
		}
		if (match || (preserveAllTokens && lastMatch)) {
			list.add(str.substring(start, i));
		}
		return (String[]) list.toArray(new String[list.size()]);
	}

	/**
	 * @param value
	 * @return
	 */
	public static boolean isEmpty(String value) {
		return value == null || value.length() == 0;
	}

	/**
	 * @param key
	 * @return
	 */
	public static boolean isNotEmpty(String value) {
		return !isEmpty(value);
	}
}
