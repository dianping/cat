package com.dianping.cat.alarm.spi.spliter;

import com.dianping.cat.alarm.spi.AlertChannel;

import java.util.regex.Pattern;

/**
 * Created by kaixi.xu on 2019/8/3 3:47 PM
 **/
public class DingTalkSpliter implements Spliter {
    public static final String ID = AlertChannel.DINGTALK.getName();

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String process(String content) {
        String dingTalkContent = content.replaceAll("<br/>", "\n");
        dingTalkContent = Pattern.compile("<div.*(?=</div>)</div>", Pattern.DOTALL).matcher(dingTalkContent).replaceAll("");
        dingTalkContent = Pattern.compile("<table.*(?=</table>)</table>", Pattern.DOTALL).matcher(dingTalkContent).replaceAll("");

        return dingTalkContent;
    }
}