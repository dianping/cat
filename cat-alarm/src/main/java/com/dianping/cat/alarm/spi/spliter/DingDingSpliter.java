package com.dianping.cat.alarm.spi.spliter;

import com.dianping.cat.alarm.spi.AlertChannel;

/**
 * @author AntzUhl
 * @Date 2020/12/7 21:00
 * @Description
 */
public class DingDingSpliter implements Spliter {

    public static final String ID = AlertChannel.DINGDING.getName();

    @Override
    public String process(String content) {
        if (content.length() > 2000) {
            content = content.substring(0, 2000) + "...";
        }
        return content;
    }

    @Override
    public String getID() {
        return ID;
    }
}
