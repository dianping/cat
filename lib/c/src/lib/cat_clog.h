#ifndef CAT_CLIENT_C_CLOG_H
#define CAT_CLIENT_C_CLOG_H

#include <lib/headers.h>

#define CLOG_DEBUG 0x01
#define CLOG_INFO   0x02
#define CLOG_WARNING 0x04
#define CLOG_ERROR 0x08
#define CLOG_CRITICAL 0x10
#define CLOG_ALL 0xFF

extern int g_log_debug;

#define INNER_LOG(type, fmt, ...) CLogLogWithLocation(type, fmt, __FILE__, __LINE__, __FUNCTION__, ##__VA_ARGS__)

// deprecated, avoid to use this.
void CLogLog(uint16_t type, const char *format, ...);

void CLogLogWithLocation(uint16_t type, const char* format, const char* file, int line, const char* function, ...);

static inline void _CLog_debugInfo(const char *fmt, ...) {
    if (g_log_debug) {
        va_list args;
        va_start(args, fmt);
        vprintf(fmt, args);
        va_end(args);
    }
}

static inline void _CLog_dateSuffix(char *tmp, int size) {
    time_t t = time(0);
    strftime(tmp, size, "_%Y_%m_%d", localtime(&t));
}

static inline void _CLog_timeSuffix(char *tmp, size_t size) {
    time_t t = time(0);
    strftime(tmp, size, "%Y-%m-%d %X ", localtime(&t));
}

#endif //CAT_CLIENT_C_CLOG_H

