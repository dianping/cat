package com.dianping.cat.util;

public class UrlParser {
    private static final char SPLIT = '/';

    public static String format(String url) {
        int length = url.length();
        StringBuilder sb = new StringBuilder(length);

        for (int index = 0; index < length; ) {
            char c = url.charAt(index);

            if (c == SPLIT && index < length - 1) {
                sb.append(c);

                StringBuilder nextSection = new StringBuilder();
                boolean isNumber = false;
                boolean first = true;

                for (int j = index + 1; j < length; j++) {
                    char next = url.charAt(j);

                    if ((first || isNumber) && next != SPLIT) {
                        isNumber = isNumber(next);
                        first = false;
                    }

                    if (next == SPLIT) {
                        if (isNumber) {
                            sb.append("{num}");
                        } else {
                            sb.append(nextSection.toString());
                        }
                        index = j;

                        break;
                    } else if (j == length - 1) {
                        if (isNumber) {
                            sb.append("{num}");
                        } else {
                            nextSection.append(next);
                            sb.append(nextSection.toString());
                        }
                        index = j + 1;
                        break;
                    } else {
                        nextSection.append(next);
                    }
                }
            } else {
                sb.append(c);
                index++;
            }
        }

        return sb.toString();
    }

    private static boolean isNumber(char c) {
        return (c >= '0' && c <= '9') || c == '.' || c == '-' || c == ',';
    }
}