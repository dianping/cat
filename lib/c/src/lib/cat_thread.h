//
// Created by Terence on 2018/8/22.
//

#ifndef CCAT_CAT_THREAD_H
#define CCAT_CAT_THREAD_H

#include "lib/headers.h"

#ifdef _WIN32

typedef HANDLE pthread_t;

#define PTHREAD DWORD WINAPI

#define pthread_create(thrp, attr, func, arg)                               \
    (((*(thrp) = CreateThread(NULL, 0,                                     \
        (LPTHREAD_START_ROUTINE)(func), (arg), 0, NULL)) == NULL) ? -1 : 0)

#define pthread_join(thr, statusp)                                          \
    ((WaitForSingleObject((thr), INFINITE) == WAIT_OBJECT_0) &&            \
    ((statusp == NULL) ? 0 :                            \
    (GetExitCodeThread((thr), (LPDWORD)(statusp)) ? 0 : -1)))

#else

#define PTHREAD void*
#define PVOID void*

#endif

static inline pid_t cat_get_current_thread_id() {
    pid_t pid = 0;
#if defined(__linux__)
    pid = syscall(SYS_gettid);
#elif defined(__APPLE__) && defined(__MACH__)
    uint64_t tid64;
    pthread_threadid_np(NULL, &tid64);
    pid = (pid_t)tid64;
#endif
    return pid;
}

static inline void cat_set_thread_name(const char* name) {
}

#ifdef WIN32
#define CATTHREADLOCAL __declspec(thread)
#elif defined(__linux__) || defined(__APPLE__)
#define CATTHREADLOCAL __thread
#else
#define CATTHREADLOCAL
#endif

#endif //CCAT_CAT_THREAD_H
