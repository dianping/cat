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
#include "cat_clog.h"

#include "lib/headers.h"
#include "lib/cat_time_util.h"
#include "lib/cat_mutex.h"

typedef struct _CLogInner {
    /** @brief    The file ptr. */
    FILE *m_f_logOut;
} CLogInner;

// Enable debug mode (print lineno, filename, funcname to stdout)
int g_log_debug = 0;

// g_log_save_filepath is required to save log.
int g_log_saveFlag = 1;

// Add created time suffix to saved logfile.
int g_log_file_with_time = 0;

// Rotate logfile everyday.
int g_log_file_perDay = 1;

// Log won't be printed unless LOG_LEVEL & g_log_permissionOpt > 0
int g_log_permissionOpt = CLOG_ALL;

#if defined(__linux__) || defined(__APPLE__)
char *g_log_save_filepath = "/data/applogs/cat/cat";
#else
char *g_log_save_filepath = "CatInnerLog";
#endif

static CLogInner *g_innerLog = NULL;
static CATCRITICALSECTION g_logCritSection = NULL;

static char CDEBUG_STR[16] = "[DEBUG] ";
static char CINFO_STR[16] = "[INFO] ";
static char CWARNING_STR[16] = "[WARNING] ";
static char CERROR_STR[16] = "[ERROR] ";
static char CUNKNOWN_STR[16] = "[UNKNOWN] ";

static long long g_log_nowDay = 0;

static void _CLog_log(uint16_t type, const char *buf);

static int CLogUpdateSaveFile() {
    if (g_log_saveFlag) {
        if (NULL != g_innerLog->m_f_logOut) {
            fclose(g_innerLog->m_f_logOut);
            g_innerLog->m_f_logOut = NULL;
        }

        char logName[512] = {'\0'};
        strncat(logName, g_log_save_filepath, 256);
        if (g_log_file_perDay) {
            _CLog_dateSuffix(logName + strlen(logName), 128);
        }
        if (g_log_file_with_time) {
            strncat(logName, GetDetailTimeString(0), 64);
        }
        strncat(logName, ".log", 64);

        g_innerLog->m_f_logOut = fopen(logName, "a+");
        if (NULL == g_innerLog->m_f_logOut) {
            _CLog_debugInfo("Log file has been opened in write mode by other process.\n");
            return -1;
        } else {
            char tmp[256];
            snprintf(tmp, 255, "Writing log to file %s.\n", logName);
            _CLog_debugInfo(tmp);
            return 1;
        }
    }
    return 1;
}

static void CLogInit() {
    g_logCritSection = CATCreateCriticalSection();
    CATCS_ENTER(g_logCritSection);
    if (g_innerLog == NULL) {
        g_innerLog = (CLogInner *) malloc(sizeof(CLogInner));
        memset(g_innerLog, 0, sizeof(CLogInner));
        g_log_nowDay = GetTime64() / 1000 / 3600;
        if (CLogUpdateSaveFile() > 0) {
            CLogLogWithLocation(CLOG_INFO, "Cat log module has been successfully initialized.", __FILE__, __LINE__, __FUNCTION__);
        }
    }
    CATCS_LEAVE(g_logCritSection);
}

void CLogLog(uint16_t type, const char *format, ...) {
    if ((type & g_log_permissionOpt) == 0) {
        return;
    }
    if (g_innerLog == NULL) {
        CLogInit();
    }
    char szBuffer[1024];
    int maxlen = 1023;
    char *printBuf = szBuffer;

    va_list args;
    va_start(args, format);
    vsnprintf(printBuf, maxlen, format, args);
    va_end(args);

    szBuffer[maxlen] = '\0';
    _CLog_log(type, szBuffer);
}

void CLogLogWithLocation(uint16_t type, const char* format, const char* file, int line, const char* function, ...) {
    if ((type & g_log_permissionOpt) == 0) {
        return;
    }
    if (g_innerLog == NULL) {
        CLogInit();
    }
    char szBuffer[1024];
    int maxlen = 1023;
    char *printBuf = szBuffer;

    if (g_log_debug) {
        char location[256];
        snprintf(location, 255, "File: \"%s\", Line %d, in %s\n", file, line, function);
        printf(location);
    }

    va_list args;
    va_start(args, function);
    vsnprintf(printBuf, maxlen, format, args);
    va_end(args);
    szBuffer[maxlen] = '\0';
    _CLog_log(type, szBuffer);
}

void _CLog_log(uint16_t type, const char *buf) {
    char tmpTime[64] = {0};
    const char *tmpType = NULL;
    char tmpBuf[1024 + 128] = {0};
    _CLog_timeSuffix(tmpTime, sizeof(tmpTime));

    switch (type) {
        case CLOG_DEBUG:
            tmpType = CDEBUG_STR;
            break;
        case CLOG_INFO:
            tmpType = CINFO_STR;
            break;
        case CLOG_WARNING:
            tmpType = CWARNING_STR;
            break;
        case CLOG_ERROR:
            tmpType = CERROR_STR;
            break;
        default:
            tmpType = CUNKNOWN_STR;
            break;
    }

    snprintf(tmpBuf, 1024 + 128, "%s%s%s\n", tmpTime, tmpType, buf);
    _CLog_debugInfo(tmpBuf);

    long long nowDay = GetTime64() / 1000 / 3600;

    CATCS_ENTER(g_logCritSection);
    if (g_log_file_perDay && nowDay > g_log_nowDay) {
        CLogUpdateSaveFile();
        g_log_nowDay = nowDay;
    }
    CATCS_LEAVE(g_logCritSection);

    if (g_log_saveFlag) {
        if (NULL != g_innerLog->m_f_logOut) {
            fputs(tmpBuf, g_innerLog->m_f_logOut);
            fflush(g_innerLog->m_f_logOut);
        }
    }
}
