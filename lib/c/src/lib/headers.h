//
// Created by Terence on 2018/8/23.
//

#ifndef CCAT_GLOBAL_H
#define CCAT_GLOBAL_H

#include <errno.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#ifdef WIN32

#include <winsock2.h>
#include <sys/timeb.h>
#include <windows.h>
#include <process.h>

#elif defined(__linux__)

#include <assert.h>
#include <errno.h>
#include <fcntl.h>
#include <ifaddrs.h>
#include <ifaddrs.h>
#include <limits.h>
#include <netdb.h>
#include <pthread.h>
#include <signal.h>
#include <signal.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>

#include <sys/ioctl.h>
#include <sys/ipc.h>
#include <sys/sem.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/syscall.h>
#include <sys/time.h>
#include <sys/timeb.h>
#include <sys/times.h>
#include <sys/types.h>

#define _GNU_SOURCE

#include <arpa/inet.h>
#include <net/if.h>
#include <netinet/in.h>
#include <netinet/tcp.h>

#elif defined(__APPLE__)

#include <assert.h>
#include <ifaddrs.h>
#include <pthread.h>
#include <zconf.h>
#include <netdb.h>

#include <arpa/inet.h>
#include <net/if.h>

#include <sys/socket.h>
#include <sys/syscall.h>
#include <sys/time.h>

#endif

#endif //CCAT_GLOBAL_H
