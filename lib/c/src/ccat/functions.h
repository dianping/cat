//
// Created by Terence on 2018/9/3.
//

#ifndef CCAT_FUNCTION_H
#define CCAT_FUNCTION_H

#include "lib/headers.h"

char *catItoA(int val, char *buf, int radix);

int catAtoI(char *buf, int radix, int *pVal);

#endif //CCAT_FUNCTION_H
