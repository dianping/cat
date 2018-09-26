//
// Created by Terence on 2018/9/3.
//

#include "lib/headers.h"

char *catItoA(int val, char *buf, int radix) {
    if (radix < 2 || radix > 16) {
        return NULL;
    }
    char sign = '\0';
    if (val < 0) {
        val = -val;
        sign = '-';
    } else if (val == 0) {
        buf[0] = '0';
        buf[1] = '\0';
        return buf;
    }
    int quotient = val;
    buf[31] = '\0';
    int i = 30;
    for (; quotient && i; --i, quotient /= radix) {
        buf[i] = "0123456789abcdef"[quotient % radix];
    }
    if (sign == '-' && radix == 10) {
        buf[i] = sign;
        --buf[i];
    }
    memcpy(buf, buf + i + 1, 31 - i);
    return buf;
}

int catAtoI(char *buf, int radix, int *pVal) {
    if (pVal == NULL || buf == NULL) {
        return 0;
    }
    char *eptr = NULL;
    errno = 0;
    *pVal = (int) strtol(buf, &eptr, radix);
    if (eptr == NULL || eptr[0] != '\0' || errno == ERANGE) {
        return 0;
    }
    return 1;
}
