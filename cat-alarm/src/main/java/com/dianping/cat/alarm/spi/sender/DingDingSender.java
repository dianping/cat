package com.dianping.cat.alarm.spi.sender;

import com.dianping.cat.alarm.spi.AlertChannel;

import java.util.List;

public class DingDingSender extends AccessTokenSender {

    public static final String ID = AlertChannel.DINGDING.getName();

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean send(SendMessageEntity message) {
        com.dianping.cat.alarm.sender.entity.Sender sender = querySender();
        boolean result = false;

        List<String> dingTalkTokens = message.getReceivers();

        for (String dingTalkToken : dingTalkTokens) {
            boolean success = sendMessage(message, dingTalkToken, sender);
            result = result || success;
        }

        return result;
    }

}
