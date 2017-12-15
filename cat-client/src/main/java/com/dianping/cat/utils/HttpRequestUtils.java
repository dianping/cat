package com.dianping.cat.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * 获取客户端实际ip
 */
public class HttpRequestUtils {

    public static String getAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");

        if (ip != null && ip.length() != 0 && !"unKnown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("X-Forwarded-For");

        if (ip != null && ip.length() != 0 && !"unKnown".equalsIgnoreCase(ip)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
