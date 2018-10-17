//
// Created by Terence on 2018/9/3.
//

#ifndef NODECAT_DEBUG_H
#define NODECAT_DEBUG_H

#include <iostream>

#define DEBUG_MODE 1

#ifdef DEBUG_MODE

#define debuginfo(s) std::cout << s << std::endl;

#else

#define debuginfo(s)

#endif

#endif //NODECAT_DEBUG_H
