package com.dianping.cat.storage;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageBucket extends Bucket<MessageTree>, TagThreadSupport<MessageTree> {

}
