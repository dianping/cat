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
