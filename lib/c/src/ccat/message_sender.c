/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include "message_sender.h"

#include "client_config.h"
#include "encoder.h"
#include "message_aggregator.h"
#include "message_manager.h"
#include "server_connection_manager.h"

#include <lib/cat_mpsc_queue.h>
#include <lib/cat_clog.h>
#include <lib/cat_time_util.h>
#include <lib/cat_thread.h>

static struct {
    CatMPSCQueue *normal;
    CatMPSCQueue *high;
    unsigned long long normalFullCount;
} g_cat_mq = {NULL, NULL, 0};

static volatile int g_cat_senderStop = 0;

volatile int g_cat_send_fd = -1;
volatile char g_cat_send_ip[64] = {0};
volatile unsigned short g_cat_send_port = 0;
volatile unsigned long long g_cat_send_blockTimes = 0;
volatile int g_cat_send_failedFlag = 0;

#define CAT_MERGEBUF_COUNT 16
#define CAT_MERGEBUF_SIZE (60 * 1024)

static pthread_t g_cat_senderHandle = NULL;
static sds g_cat_mergeBuf = NULL;

extern CatEncoder *g_cat_encoder;

extern CatMessageManager g_cat_messageManager;

int isCatSenderEnable() {
    return g_cat_send_fd > 0;
}

static inline int mqOffer(CatMessageTree *tree) {
    if (tree->canDiscard) {
        if (CatMPSC_offer(g_cat_mq.normal, tree) != 0) {
            if (g_config.enableSampling) {
                INNER_LOG(CLOG_WARNING, "normal queue is full, message has been aggregated.");
                sendToAggregator(tree);
            } else {
                INNER_LOG(CLOG_ERROR, "normal queue is full, message has been discarded!");
            }
            deleteCatMessageTree(tree);
            return 0;
        }
    } else {
        if (CatMPSC_offer(g_cat_mq.high, tree) != 0) {
            INNER_LOG(CLOG_ERROR, "high queue is full, message has been discarded!");
            deleteCatMessageTree(tree);
            return 1;
        }
    }
    return 0;
}

static inline int mqPollMany(void **bufArray, int max) {
    int current = 0;
    void* data;

    // poll from high priority queue non-blocking-ly
    while (current < max) {
        data = CatMPSC_poll(g_cat_mq.high);
        if (NULL == data) {
            break;
        }
        bufArray[current++] = data;
    }

    // poll from normal priority queue non-blocking-ly
    while (current < max) {
        data = CatMPSC_poll(g_cat_mq.normal);
        if (NULL == data) {
            break;
        }
        bufArray[current++] = data;
    }

    // if element num doesn't reach max, poll from normal queue blocking-ly
    // to avoid of endless loop.
    if (current < max) {
        data = CatMPSC_bpoll(g_cat_mq.normal, 5);
        if (NULL != data) {
            bufArray[current++] = data;
        }
    }
    return current;
}

/**
 *
 * @param tree
 * @return int
 */
int sendRootMessage(CatMessageTree *tree) {
    if (NULL == tree) {
        return 0;
    }

    if (!tree->canDiscard) {
        return mqOffer(tree);
    } else if (g_config.enableSampling && hitSample()) {
        return mqOffer(tree);
    } else {
        sendToAggregator(tree);
        deleteCatMessageTree(tree);
        return 0;
    }
}

static int sendCatMessageBufferDirectly(sds sendBuf, size_t checkpoint) {
    if (g_cat_send_failedFlag) {
        return -1;
    }

    if (g_cat_send_fd < 0) {
        INNER_LOG(CLOG_WARNING, "当前ip不可用: %s, 查找其他可用机器。", g_cat_send_ip);
        recoverCatServerConn();
        if (g_cat_send_fd < 0) {
            return -1;
        }
    }

    size_t sendTotalLen = checkpoint;
    ssize_t nowSendLen = 0;
    ssize_t sendLen = 0;

    while (nowSendLen != sendTotalLen) {
#ifdef WIN32
        sendLen = send(g_cat_send_fd, buf, sendTotalLen - nowSendLen, 0);
#else
        // write()会把参数buf 所指的内存写入count 个字节到参数fd 所指的文件内. 当然, 文件读写位置也会随之移动.
        sendLen = write(g_cat_send_fd, sendBuf, sendTotalLen - nowSendLen);
#endif

        if (sendLen == -1) {
#ifdef WIN32
            if (WSAGetLastError() == WSAEWOULDBLOCK)
#else
            if (errno == EAGAIN)
#endif
            {
                ++g_cat_send_blockTimes;
                if (g_cat_send_blockTimes % 1000000 == 0) {
                    INNER_LOG(CLOG_WARNING, "Send Cat Message : %s is blocking.", g_cat_send_ip);
                }
                // if we send nothing, we can break, otherwise we must send the last buffer to server.
                // if we don't do this, the server may receive uncompleted package
                if (nowSendLen == 0) {
                    INNER_LOG(CLOG_DEBUG, "Tcp buffer is full, message has been discarded");
                    break;
                }

                Sleep(5);
                continue;

            } else {
                INNER_LOG(CLOG_WARNING, "Send to server :  %s failed.", g_cat_send_ip);
                nowSendLen = -1;
                break;
            }
        }
        nowSendLen += sendLen;
        sendBuf += sendLen;
    }

    if (nowSendLen < 0) {
        INNER_LOG(CLOG_WARNING, "当前ip发送失败: %s, 尝试恢复。", g_cat_send_ip);
        recoverCatServerConn();

        if (g_cat_send_fd < 0) {
            INNER_LOG(CLOG_ERROR, "Recover failed.");
        }
    } else {

    }

    return 1;
}

static sds catsdsrotate(sds s, size_t offset) {
    sdshdr *sh = (sdshdr*) (s - (sizeof(sdshdr)));

    if (offset > sh->len) {
        catsdsclear(s);
        return s;
    }

    size_t i;
    char *l, *r;
    for (i = offset, l = s, r = s + offset; i <= sh->len; i++, l++, r++) {
        *l = *r;
    }
    sh->len -= offset;
    sh->free += offset;
    return s;
}

static PTHREAD catMessageSenderFun(PVOID para) {
    cat_set_thread_name("cat-sender");

    CatMessageTree *messageArray[CAT_MERGEBUF_COUNT];
    g_cat_mergeBuf = catsdsnewEmpty(CAT_MERGEBUF_SIZE);

    while (!g_cat_senderStop) {
        catsdsclear(g_cat_mergeBuf);

        int eleNum = mqPollMany((void **) messageArray, CAT_MERGEBUF_COUNT);
        if (eleNum == 0) {
            continue;
        }

        size_t checkpoint = 0;
        for (int i = 0; i < eleNum; i++) {
            catEncodeMessageTree(messageArray[i], g_cat_mergeBuf);
            deleteCatMessageTree(messageArray[i]);

            if (catsdslen(g_cat_mergeBuf) >= CAT_MERGEBUF_SIZE) {
                sendCatMessageBufferDirectly(g_cat_mergeBuf, checkpoint);
                g_cat_mergeBuf = catsdsrotate(g_cat_mergeBuf, checkpoint);
            }
            checkpoint = catsdslen(g_cat_mergeBuf);
        }

        if (checkpoint > 0) {
            sendCatMessageBufferDirectly(g_cat_mergeBuf, checkpoint);
        }
    }
    return 0;
}

void initCatSenderThread() {
    g_cat_mergeBuf = catsdsnewEmpty(2 * 1024 * 1024);

    switch (g_config.encoderType) {
        case CAT_ENCODER_BINARY:
            g_cat_encoder = newCatBinaryEncoder();
            break;
        case CAT_ENCODER_TEXT:
            g_cat_encoder = newCatTextEncoder();
            break;
        default:
            INNER_LOG(CLOG_ERROR, "cat encoder has not been specified!");
            return;
    }

    g_cat_encoder->setAppkey(g_cat_encoder, g_cat_messageManager.domain);
    g_cat_encoder->setHostname(g_cat_encoder, g_cat_messageManager.hostname);
    g_cat_encoder->setIp(g_cat_encoder, g_cat_messageManager.ip);

    g_cat_mq.normal = newCatMPSCQueue("sender_normal", g_config.messageQueueSize);
    catChecktPtr(g_cat_mq.normal);

    g_cat_mq.high = newCatMPSCQueue("sender_high", g_config.messageQueueSize);
    catChecktPtr(g_cat_mq.high);

    g_cat_senderStop = 0;
    pthread_create(&g_cat_senderHandle, NULL, catMessageSenderFun, NULL);
}

static void clearMessageQueue(CatMPSCQueue* q) {
    CatMessageTree* tree;
    while (NULL != (tree = CatMPSC_poll(q))) {
        deleteCatMessageTree(tree);
    }
    deleteCatMPSCQueue(q);
}

void clearCatSenderThread() {
    g_cat_senderStop = 1;
    pthread_join(g_cat_senderHandle, NULL);

    clearMessageQueue(g_cat_mq.normal);
    clearMessageQueue(g_cat_mq.high);

    catsdsfree(g_cat_mergeBuf);
}
