#include "message_manager.h"

#include "ccat/client_config.h"
#include "ccat/context.h"
#include "ccat/message_id.h"
#include "ccat/message_sender.h"

#include "lib/cat_clog.h"
#include "lib/cat_network_util.h"

CatMessageManager g_cat_messageManager = {0}; //memset(&g_cat_messageManager, 0, sizeof(g_cat_messageManager))

void catMessageManagerAdd(CatMessage *message) {
    catContextAdd(message);
}

void catMessageManagerEndTrans(CatTransaction *message) {
    if (catContextEndTrans(message)) {
        resetCatContext();
    }
}

void catMessageManagerFlush(CatMessageTree *rootMsg) {
    // CatMessageTree * pRootMsg = getContextMessageTree();
    if (NULL == rootMsg->messageId) {
        rootMsg->messageId = getNextMessageId();
    }
    if (isCatSenderEnable() && g_config.messageEnableFlag) {
        sendRootMessage(rootMsg);
    } else {
        deleteCatMessageTree(rootMsg);
        ++g_cat_messageManager.throttleTimes;
        if (g_cat_messageManager.throttleTimes == 1 || g_cat_messageManager.throttleTimes % 1000000 == 0) {
            INNER_LOG(CLOG_WARNING, "Cat Message is throttled! Times: %d", g_cat_messageManager.throttleTimes);
        }
    }
}

void initMessageManager(const char *domain, const char *hostName) {
    g_cat_messageManager.domain = catsdsnew(domain);
    catChecktPtr(g_cat_messageManager.domain);

    g_cat_messageManager.hostname = catsdsnew(hostName);
    catChecktPtr(g_cat_messageManager.hostname);

    g_cat_messageManager.ip = catsdsnewEmpty(64);
    catChecktPtr(g_cat_messageManager.ip);
    getLocalHostIp(g_cat_messageManager.ip);

    // Determine if ip has been got successfully.
    if (g_cat_messageManager.ip[0] == '\0') {
        INNER_LOG(CLOG_WARNING, "Cannot get self ip address, using default ip: %s", g_config.defaultIp);
        g_cat_messageManager.ip = catsdscpy(g_cat_messageManager.ip, g_config.defaultIp);
    }
    INNER_LOG(CLOG_INFO, "Current ip: %s", g_cat_messageManager.ip);

    g_cat_messageManager.ipHex = catsdsnewEmpty(64);
    catChecktPtr(g_cat_messageManager.ipHex);
    getLocalHostIpHex(g_cat_messageManager.ipHex);

    // Determine if ipX has been got successfully.
    if (g_cat_messageManager.ipHex[0] == '\0') {
        INNER_LOG(CLOG_WARNING, "Cannot get self ip address, using default ip hex: %s", g_config.defaultIpHex);
        g_cat_messageManager.ipHex = catsdscpy(g_cat_messageManager.ipHex, g_config.defaultIpHex);
    }
    INNER_LOG(CLOG_INFO, "Current ip hex: %s", g_cat_messageManager.ipHex);
}

void catMessageManagerDestroy() {
    catsdsfree(g_cat_messageManager.domain);
    g_cat_messageManager.domain = NULL;
    catsdsfree(g_cat_messageManager.hostname);
    g_cat_messageManager.hostname = NULL;
    catsdsfree(g_cat_messageManager.ip);
    g_cat_messageManager.ip = NULL;
    catsdsfree(g_cat_messageManager.ipHex);
    g_cat_messageManager.ipHex = NULL;
}

void catMessageManagerStartTrans(CatTransaction *trans) {
    catContextStartTrans(trans);
}

