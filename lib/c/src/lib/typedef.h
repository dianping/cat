//
// Created by Terence on 2018/8/16.
//

#ifndef CCAT_TYPEDEF_H
#define CCAT_TYPEDEF_H

#if defined WIN32

typedef unsigned char u_char;
typedef unsigned char u_int8;
typedef unsigned short u_int16;
typedef unsigned int u_int32;
typedef unsigned __int64 u_int64;
typedef signed char int8;
typedef signed short int16;
typedef signed int int32;
typedef signed __int64 int64;

#elif defined __linux__ || defined __APPLE__

typedef unsigned char u_char;
typedef unsigned char u_int8;
typedef unsigned short u_int16;
typedef unsigned int u_int32;
typedef unsigned long long u_int64;
typedef signed char int8;
typedef signed short int16;
typedef signed int int32;
typedef long long int64;

#endif

#endif //CCAT_TYPEDEF_H
