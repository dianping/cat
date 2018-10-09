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
#include "monitor.h"

#include "ccat/client_config.h"
#include "ccat/message_id.h"
#include "ccat/message_manager.h"
#include "ccat/monitor_collector.h"
#include "ccat/server_connection_manager.h"
#include "ccat/version.h"

#include "lib/cat_thread.h"
#include "lib/cat_time_util.h"

extern CatMessageManager g_cat_messageManager;

static volatile int g_cat_monitorStop = 0;

static pthread_t g_cat_monitorHandle;

CatClientInfo g_client_info = {"C", ""};

static PTHREAD catMonitorFun(PVOID para) {
    Sleep(1000);
    CatTransaction *reboot = newTransaction("System", "Reboot");

    logEvent("Reboot", g_cat_messageManager.ip, CAT_SUCCESS, NULL);

    reboot->setStatus(reboot, CAT_SUCCESS);
    reboot->complete(reboot);

    unsigned long runCount = 1;
    while (!g_cat_monitorStop) {

        // check connection status every 1s.
        checkCatActiveConn();

        if (runCount % 10 == 1) {
            saveMark();
        }

        // check router list every 3m.
        if (runCount % 180 == 0) {
            updateCatServerConn();
        }

        if (runCount % 60 == 1 && g_config.enableHeartbeat) {
            // Report ccat version.
            logEvent("Cat_C_Client_Version", CCAT_VERSION, CAT_SUCCESS, NULL);

            // Report vm / runtime version. (For other programming language which using ccat mixin to report heartbeat)
            if (strcmp(g_client_info.language, "C") != 0) {
                sds name = catsdsnew("");
                name = catsdscatfmt(name, "Cat_%s_Client_Version", g_client_info.language);
                logEvent(name, g_client_info.language_version, CAT_SUCCESS, NULL);
                catsdsfree(name);
            }

            // Report system status
            CatTransaction *t = newTransaction("System", "Status");

            CatHeartBeat *h = newHeartBeat("Heartbeat", g_cat_messageManager.ip);
            char *xmlContent = get_status_report();
            h->addData(h, xmlContent);
            free(xmlContent);
            h->complete(h);

            t->setStatus(t, CAT_SUCCESS);
            t->complete(t);
        }
        runCount++;

        Sleep(1000);
    }
    return 0;
}

void initCatMonitorThread() {
    g_cat_monitorStop = 0;
    pthread_create(&g_cat_monitorHandle, NULL, catMonitorFun, NULL);
}

void clearCatMonitor() {
    g_cat_monitorStop = 1;
    pthread_join(g_cat_monitorHandle, NULL);
}

