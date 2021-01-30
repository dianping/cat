package com.dianping.cat.alarm.spi.sender;

import com.dianping.cat.alarm.spi.AlertChannel;

import java.util.List;

public class QywxSender extends AccessTokenSender {

    public static final String ID = AlertChannel.QYWX.getName();

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean send(SendMessageEntity message) {
        com.dianping.cat.alarm.sender.entity.Sender sender = querySender();
        boolean result = false;

        List<String> qywxTokens = message.getReceivers();

        for (String qywxToken : qywxTokens) {
            boolean success = sendMessage(message, qywxToken, sender);
            result = result || success;
        }

        return result;
    }

}
