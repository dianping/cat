#ifndef CAT_CLIENT_C_CLIENT_COMMON_H
#define CAT_CLIENT_C_CLIENT_COMMON_H

#include <string.h>
#include <stdlib.h>
#include <errno.h>

/**
 * client export
 */
#ifdef WIN32

#ifdef  CCATCLIENT_EXPORTS
#define  CATCLIENT_EXPORT __declspec(dllexport)
#else
#define  CATCLIENT_EXPORT __declspec(dllimport)
#endif

#else

#define CATCLIENT_EXPORT

#endif

/**
 * inline
 */
#ifdef WIN32
#define inline __inline
#endif

#define CAT_SUCCESS "0"
#define CAT_SUCCESS_CHAR '0'
#define CAT_ERROR "-1"
#define CAT_FAIL "FAIL"

#define catChecktPtr(ptr) catChecktPtrWithName((ptr), (#ptr))

int isCatEnabled();

void catChecktPtrWithName(void *, char *);

static inline unsigned long long catTrimToHour(unsigned long long timeMs) {
    return timeMs / (3600 * 1000);
}

// buf size must >= 32
static inline char * catItoA(int val, char * buf, int radix)
{
    if (radix < 2 || radix > 16)
    {
        return NULL;
    }
    char * out = buf;
    char sign = '\0';
    if (val < 0)
    {
        val = -val;
        sign = '-';
    }
    else if (val == 0)
    {
        buf[0] = '0';
        buf[1] = '\0';
        return buf;
    }
    int quotient = val;
    buf[31] = '\0';
    int i = 30;
    for (; quotient && i; --i, quotient /= radix)
    {
        buf[i] = "0123456789abcdef"[quotient % radix];
    }
    if (sign == '-' && radix == 10)
    {
        buf[i] = sign;
        --buf[i];
    }
    memcpy(buf, buf + i + 1, 31 - i);
    return buf;
}

static inline int catAtoI(char * buf, int radix, int * pVal)
{
    if (pVal == NULL || buf == NULL)
    {
        return 0;
    }
    char *eptr = NULL;
    errno = 0;
    *pVal = strtol(buf, &eptr, radix);
    if (eptr == NULL || eptr[0] != '\0' || errno == ERANGE)
    {
        return 0;
    }
    return 1;
}

#ifdef WIN32
#define CATTHREADLOCAL __declspec(thread)
#elif defined(__linux__) || defined(__APPLE__)
#define CATTHREADLOCAL __thread
#else
#define CATTHREADLOCAL
#endif


#endif //CAT_CLIENT_C_CLIENT_COMMON_H
