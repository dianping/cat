package com.dianping.cat.configuration;

import com.dianping.cat.util.Splitters;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MessageTreeTypeParser {
    private ConcurrentHashMap<String, MessageType> messageTypes = new ConcurrentHashMap<String, MessageType>();
    private List<String> startTypes;
    private List<String> matchTypes;
    private String lastStartType;
    private String lastMatchType;

    public MessageType parseMessageType(MessageTree tree) {
        Message message = tree.getMessage();

        if (message instanceof Transaction) {
            String type = message.getType();
            MessageType messageType = messageTypes.get(type);

            if (messageType != null) {
                return messageType;
            } else {
                if (startTypes != null) {
                    for (String s : startTypes) {
                        if (type.startsWith(s)) {
                            messageType = MessageType.SMALL_TRANSACTION;
                            break;
                        }
                    }
                }
                if (matchTypes != null && matchTypes.contains(type)) {
                    messageType = MessageType.SMALL_TRANSACTION;
                }
                if (messageType == null) {
                    messageType = MessageType.NORMAL_MESSAGE;
                }

                messageTypes.put(type, messageType);

                return messageType;
            }
        } else if (message instanceof Event) {
            return MessageType.STAND_ALONE_EVENT;
        } else {
            return MessageType.NORMAL_MESSAGE;
        }
    }

    public void refresh(String startTypes, String matchTypes) {
        if (startTypes != null && !startTypes.equals(lastStartType)) {
            this.startTypes = Splitters.by(";").noEmptyItem().split(startTypes);
            lastStartType = startTypes;
        }

        if (matchTypes != null && !matchTypes.equals(lastMatchType)) {
            this.matchTypes = Splitters.by(";").noEmptyItem().split(matchTypes);
            lastMatchType = matchTypes;
        }
    }
}
