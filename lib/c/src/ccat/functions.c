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
