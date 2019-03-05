/* anet.c -- Basic TCP socket stuff made a bit less boring
 *
 * Copyright (c) 2006-2012, Salvatore Sanfilippo <antirez at gmail dot com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of Redis nor the names of its contributors may be used
 *     to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */


#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <errno.h>
#include <stdarg.h>
#include <stdio.h>

#ifdef WIN32

#include <winsock2.h>
#include <sys/timeb.h>
#include <sys/types.h>

#include <time.h>

#include <WS2tcpip.h>
#include <windows.h>

// unsigned long inet_aton(char * ipStr, unsigned long * addr)
// {
//     *addr = inet_addr(ipStr);
//     if (*addr == INADDR_NONE)
//     {
//         return 0;
//     }
//     return 1;
// }
#define close closesocket

#define snprintf sprintf_s

#else


#include <sys/socket.h>
#include <sys/un.h>
#include <sys/time.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <netdb.h>

#endif

#include "cat_anet.h"
#include "cat_ae.h"
#include "cat_time_util.h"

static void anetSetError(char *err, const char *fmt, ...) {
    va_list ap;

    if (!err) return;
    va_start(ap, fmt);
    vsnprintf(err, ANET_ERR_LEN, fmt, ap);
    vprintf(fmt, ap);
    va_end(ap);
}

int anetSetBlock(char *err, int fd, int non_block) {

#ifdef WIN32
    unsigned long mode = non_block;
    if (ioctlsocket(fd, FIONBIO, &mode) < 0)
    {
        printf("ioctlsocket FIONBIO error %d\n", WSAGetLastError());
        return ANET_ERR;
    }
#else

    int flags;

    /* Set the socket blocking (if non_block is zero) or non-blocking.
     * Note that fcntl(2) for F_GETFL and F_SETFL can't be
     * interrupted by a signal. */
    if ((flags = fcntl(fd, F_GETFL)) == -1) {
        anetSetError(err, "fcntl(F_GETFL): %s", strerror(errno));
        return ANET_ERR;
    }

    if (non_block)
        flags |= O_NONBLOCK;
    else
        flags &= ~O_NONBLOCK;

    if (fcntl(fd, F_SETFL, flags) == -1) {
        anetSetError(err, "fcntl(F_SETFL,O_NONBLOCK): %s", strerror(errno));
        return ANET_ERR;
    }

#endif

    return ANET_OK;
}

int catAnetNonBlock(char *err, int fd) {
    return anetSetBlock(err, fd, 1);
}

int catAnetBlock(char *err, int fd) {
    return anetSetBlock(err, fd, 0);
}

/* Set TCP keep alive option to detect dead peers. The interval option
 * is only used for Linux as we are using Linux-specific APIs to set
 * the probe send time, interval, and count. */
int catAnetKeepAlive(char *err, int fd, int interval) {
    int val = 1;

    if (setsockopt(fd, SOL_SOCKET, SO_KEEPALIVE, &val, sizeof(val)) == -1) {
        anetSetError(err, "setsockopt SO_KEEPALIVE: %s", strerror(errno));
        return ANET_ERR;
    }

#ifdef __linux__
    /* Default settings are more or less garbage, with the keepalive time
     * set to 7200 by default on Linux. Modify settings to make the feature
     * actually useful. */

    /* Send first probe after interval. */
    val = interval;
    if (setsockopt(fd, IPPROTO_TCP, TCP_KEEPIDLE, &val, sizeof(val)) < 0) {
        anetSetError(err, "setsockopt TCP_KEEPIDLE: %s\n", strerror(errno));
        return ANET_ERR;
    }

    /* Send next probes after the specified interval. Note that we set the
     * delay as interval / 3, as we send three probes before detecting
     * an error (see the next setsockopt call). */
    val = interval/3;
    if (val == 0) val = 1;
    if (setsockopt(fd, IPPROTO_TCP, TCP_KEEPINTVL, &val, sizeof(val)) < 0) {
        anetSetError(err, "setsockopt TCP_KEEPINTVL: %s\n", strerror(errno));
        return ANET_ERR;
    }

    /* Consider the socket in error state after three we send three ACK
     * probes without getting a reply. */
    val = 3;
    if (setsockopt(fd, IPPROTO_TCP, TCP_KEEPCNT, &val, sizeof(val)) < 0) {
        anetSetError(err, "setsockopt TCP_KEEPCNT: %s\n", strerror(errno));
        return ANET_ERR;
    }
#else
    ((void) interval); /* Avoid unused var warning for non Linux systems. */
#endif

    return ANET_OK;
}

static int anetSetTcpNoDelay(char *err, int fd, int val) {
    if (setsockopt(fd, IPPROTO_TCP, TCP_NODELAY, &val, sizeof(val)) == -1) {
        anetSetError(err, "setsockopt TCP_NODELAY: %s", strerror(errno));
        return ANET_ERR;
    }
    return ANET_OK;
}

int catAnetEnableTcpNoDelay(char *err, int fd) {
    return anetSetTcpNoDelay(err, fd, 1);
}

int catAnetDisableTcpNoDelay(char *err, int fd) {
    return anetSetTcpNoDelay(err, fd, 0);
}


int anetSetSendBuffer(char *err, int fd, int buffsize) {
    if (setsockopt(fd, SOL_SOCKET, SO_SNDBUF, &buffsize, sizeof(buffsize)) == -1) {
        anetSetError(err, "setsockopt SO_SNDBUF: %s", strerror(errno));
        return ANET_ERR;
    }
    return ANET_OK;
}

int catAnetTcpKeepAlive(char *err, int fd) {
    int yes = 1;
    if (setsockopt(fd, SOL_SOCKET, SO_KEEPALIVE, &yes, sizeof(yes)) == -1) {
        anetSetError(err, "setsockopt SO_KEEPALIVE: %s", strerror(errno));
        return ANET_ERR;
    }
    return ANET_OK;
}

/* Set the socket send timeout (SO_SNDTIMEO socket option) to the specified
 * number of milliseconds, or disable it if the 'ms' argument is zero. */
int catAnetSendTimeout(char *err, int fd, long long ms) {
    struct timeval tv;

    tv.tv_sec = ms / 1000;
    tv.tv_usec = (ms % 1000) * 1000;
    if (setsockopt(fd, SOL_SOCKET, SO_SNDTIMEO, &tv, sizeof(tv)) == -1) {
        anetSetError(err, "setsockopt SO_SNDTIMEO: %s", strerror(errno));
        return ANET_ERR;
    }
    return ANET_OK;
}

/* anetGenericResolve() is called by catAnetResolve() and catAnetResolveIP() to
 * do the actual work. It resolves the hostname "host" and set the string
 * representation of the IP address into the buffer pointed by "ipbuf".
 *
 * If flags is set to ANET_IP_ONLY the function only resolves hostnames
 * that are actually already IPv4 or IPv6 addresses. This turns the function
 * into a validating / normalizing function. */
int anetGenericResolve(char *err, char *host, char *ipbuf, size_t ipbuf_len,
                       int flags, int hexFlag) {
    char hostname[255];
    if (host == NULL) {
        if (gethostname(hostname, sizeof(hostname)) == 0) {
            host = hostname;
            printf("HostName : %s \n", hostname);
        } else {
            printf("GetHostName Error \n");
            return ANET_ERR;
        }
    }

#ifdef WIN32
    PHOSTENT hostinfo = NULL;
    unsigned int ipValue = 0;
    if ((hostinfo = gethostbyname(host)) != NULL)
    {
        memcpy(&ipValue, *hostinfo->h_addr_list, 4);
    }
    else
    {
        anetSetError(err, "gethostbyname error %d", WSAGetLastError());
        return ANET_ERR;
    }

    struct in_addr inaddr;
    inaddr.s_addr = ipValue;
    if (!hexFlag)
    {
        strcpy(ipbuf, inet_ntoa(inaddr));
    }
    else
    {
        unsigned char * ipValueS = (unsigned char *)&ipValue;
        for (int i = 0; i < 4; ++i)
        {
            if (ipValueS[i] > 16)
            {
                sprintf(ipbuf + (i << 1), "%lx", (long)ipValueS[i]);
            }
            else
            {

                sprintf(ipbuf + (i << 1), "%lx", (long)ipValueS[i]);
            }
        }
    }
#else

    struct addrinfo hints, *info;
    int rv;

    memset(&hints, 0, sizeof(hints));
    if (flags & ANET_IP_ONLY) hints.ai_flags = AI_NUMERICHOST;
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;  /* specify socktype to avoid dups */

    if ((rv = getaddrinfo(host, NULL, &hints, &info)) != 0) {
        anetSetError(err, "%s", gai_strerror(rv));
        printf("%s\n", gai_strerror(rv));
        return ANET_ERR;
    }
    if (info->ai_family == AF_INET) {
        struct sockaddr_in *sa = (struct sockaddr_in *) info->ai_addr;
        if (!hexFlag) {
            inet_ntop(AF_INET, &(sa->sin_addr), ipbuf, ipbuf_len);
        } else {
            unsigned char *ipValueS = (unsigned char *) (&sa->sin_addr.s_addr);
            int i = 0;
            for (; i < 4; ++i) {
                if (ipValueS[i] > 16) {
                    sprintf(ipbuf + (i << 1), "%lx", (long) ipValueS[i]);
                } else {

                    sprintf(ipbuf + (i << 1), "%lx", (long) ipValueS[i]);
                }
            }
        }
    } else {
        struct sockaddr_in6 *sa = (struct sockaddr_in6 *) info->ai_addr;
        inet_ntop(AF_INET6, &(sa->sin6_addr), ipbuf, ipbuf_len);
    }

    freeaddrinfo(info);

#endif
    return ANET_OK;
}


int catAnetResolve(char *err, char *host, char *ipbuf, size_t ipbuf_len) {
    return anetGenericResolve(err, host, ipbuf, ipbuf_len, ANET_NONE, 0);
}

int catAnetResolveIP(char *err, char *host, char *ipbuf, size_t ipbuf_len) {
    return anetGenericResolve(err, host, ipbuf, ipbuf_len, ANET_IP_ONLY, 0);
}


int catAnetGetHost(char *err, char *host, size_t ipbuf_len) {
    if (host != NULL) {
        if (gethostname(host, ipbuf_len) == 0) {
            return ANET_OK;
        } else {
        }
    }
    return ANET_ERR;
}

int catAnetResolveIPHex(char *err, char *host, char *ipbuf, size_t ipbuf_len) {
    return anetGenericResolve(err, host, ipbuf, ipbuf_len, ANET_IP_ONLY, 1);
}


static int anetSetReuseAddr(char *err, int fd) {
    int yes = 1;
    /* Make sure connection-intensive things like the redis benckmark
     * will be able to close/open sockets a zillion of times */
    if (setsockopt(fd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(yes)) == -1) {
        anetSetError(err, "setsockopt SO_REUSEADDR: %s", strerror(errno));
        return ANET_ERR;
    }
    return ANET_OK;
}

static int anetCreateSocket(char *err, int domain) {
    int s;
    if ((s = socket(domain, SOCK_STREAM, 0)) == -1) {
        anetSetError(err, "creating socket: %s", strerror(errno));
        return ANET_ERR;
    }

    /* Make sure connection-intensive things like the redis scripts
     * will be able to close/open sockets a zillion of times */
    if (anetSetReuseAddr(err, s) == ANET_ERR) {
        close(s);
        return ANET_ERR;
    }
    return s;
}

#define ANET_CONNECT_NONE 0
#define ANET_CONNECT_NONBLOCK 1

static int anetTcpGenericConnect(char *err, char *addr, int port, int flags) {
    int s = ANET_ERR, rv;
    char portstr[6];  /* strlen("65535") + 1; */
    struct addrinfo hints, *servinfo, *p;

    snprintf(portstr, sizeof(portstr), "%d", port);
    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;

    if ((rv = getaddrinfo(addr, portstr, &hints, &servinfo)) != 0) {
        anetSetError(err, "%s", gai_strerror(rv));
        return ANET_ERR;
    }
    for (p = servinfo; p != NULL; p = p->ai_next) {
        /* Try to create the socket and to connect it.
         * If we fail in the socket() call, or on connect(), we retry with
         * the next entry in servinfo. */
        if ((s = socket(p->ai_family, p->ai_socktype, p->ai_protocol)) == -1)
            continue;
        if (anetSetReuseAddr(err, s) == ANET_ERR) goto error;
        if (flags & ANET_CONNECT_NONBLOCK && catAnetNonBlock(err, s) != ANET_OK)
            goto error;
        if (connect(s, p->ai_addr, p->ai_addrlen) == -1) {
            /* If the socket is non-blocking, it is ok for connect() to
             * return an EINPROGRESS error here. */

#ifdef WIN32
            int rst = WSAGetLastError();
            if (rst == WSAEWOULDBLOCK && flags & ANET_CONNECT_NONBLOCK)
                goto end;

#else


            if (errno == EINPROGRESS && flags & ANET_CONNECT_NONBLOCK)
                goto end;

#endif
            close(s);
            s = ANET_ERR;
            continue;
        }

        /* If we ended an iteration of the for loop without errors, we
         * have a connected socket. Let's return to the caller. */
        goto end;
    }
    if (p == NULL)
        anetSetError(err, "creating socket: %s", strerror(errno));

    error:
    if (s != ANET_ERR) {
        close(s);
        s = ANET_ERR;
    }
    end:
    freeaddrinfo(servinfo);
    return s;
}

int catAnetTcpConnect(char *err, char *addr, int port) {
    return anetTcpGenericConnect(err, addr, port, ANET_CONNECT_NONE);
}

int catAnetTcpNonBlockConnect(char *err, char *addr, int port) {
    return anetTcpGenericConnect(err, addr, port, ANET_CONNECT_NONBLOCK);
}

int anetUnixGenericConnect(char *err, char *path, int flags) {
    int s = ANET_ERR;
#ifndef WIN32
    struct sockaddr_un sa;

    if ((s = anetCreateSocket(err, AF_LOCAL)) == ANET_ERR)
        return ANET_ERR;

    sa.sun_family = AF_LOCAL;
    strncpy(sa.sun_path, path, sizeof(sa.sun_path) - 1);
    if (flags & ANET_CONNECT_NONBLOCK) {
        if (catAnetNonBlock(err, s) != ANET_OK)
            return ANET_ERR;
    }
    if (connect(s, (struct sockaddr *) &sa, sizeof(sa)) == -1) {
        if (errno == EINPROGRESS &&
            flags & ANET_CONNECT_NONBLOCK)
            return s;

        anetSetError(err, "connect: %s", strerror(errno));
        close(s);
        return ANET_ERR;
    }
#endif
    return s;
}

int catAnetUnixConnect(char *err, char *path) {
    return anetUnixGenericConnect(err, path, ANET_CONNECT_NONE);
}

int catAnetUnixNonBlockConnect(char *err, char *path) {
    return anetUnixGenericConnect(err, path, ANET_CONNECT_NONBLOCK);
}

/* Like read(2) but make sure 'count' is read before to return
 * (unless error or EOF condition is encountered) */
static int anetReadWidthType(int fd, char *buf, int count, int flag, int waitMs) {
    int nread, totlen = 0;
    unsigned long long timeBegin = 0;
    unsigned long long nowTime = 0;

    if (flag == 2) {
        timeBegin = GetTime64();
        nowTime = timeBegin;
    }

    while (totlen != count) {
#ifdef WIN32
        nread = recv(fd, buf, count - totlen, 0);
#else
        nread = read(fd, buf, count - totlen);
#endif
        if (nread == 0) return totlen;
        if (nread == -1) {
#ifdef WIN32
            if (WSAGetLastError() == WSAEWOULDBLOCK)
#else
            if (errno == EAGAIN)
#endif
            {
                nread = 0;
                if (flag == 1) {
                    // wait 100 ms
                    catAeWait(fd, AE_READABLE, 100);
                } else if (flag == 2) {
                    nowTime = GetTime64();
                    if (nowTime - timeBegin > waitMs) {
                        break;
                    } else {
                        catAeWait(fd, AE_WRITABLE, waitMs - (nowTime - timeBegin));
                    }
                } else {
                    break;
                }
            } else {
                return -1;
            }
        }
        totlen += nread;
        buf += nread;
    }
    return totlen;
}

/* Like write(2) but make sure 'count' is read before to return
 * (unless error is encountered) */
static int anetWriteWidthType(int fd, char *buf, int count, int flag, int waitMs) {
    int nwritten, totlen = 0;
    unsigned long long timeBegin = 0;
    unsigned long long nowTime = 0;
    if (flag == 2) {
        timeBegin = GetTime64();
        nowTime = timeBegin;
    }
    while (totlen != count) {
#ifdef WIN32
        nwritten = send(fd, buf, count - totlen, 0);
#else
        nwritten = write(fd, buf, count - totlen);
#endif
        //if (nwritten == 0) return totlen;

        if (nwritten == -1) {

#ifdef WIN32
            if (WSAGetLastError() == WSAEWOULDBLOCK)
#else
            if (errno == EAGAIN)
#endif
            {
                nwritten = 0;
                if (flag == 1) {
                    // wait 100 ms
                    catAeWait(fd, AE_WRITABLE, 100);
                } else if (flag == 2) {

                    nowTime = GetTime64();
                    if (nowTime - timeBegin > waitMs) {
                        break;
                    } else {
                        catAeWait(fd, AE_WRITABLE, waitMs - (nowTime - timeBegin));
                    }
                } else {
                    break;
                }
            } else {
                return -1;
            }
        }
        totlen += nwritten;
        buf += nwritten;
    }
    return totlen;
}

int catAnetBlockRead(int fd, char *buf, int count) {
    return anetReadWidthType(fd, buf, count, 1, 0);
}

int catAnetBlockWrite(int fd, char *buf, int count) {
    return anetWriteWidthType(fd, buf, count, 1, 0);
}

int catAnetNoBlockRead(int fd, char *buf, int count) {
    return anetReadWidthType(fd, buf, count, 0, 0);

}

int catAnetNoBlockWrite(int fd, char *buf, int count) {
    return anetWriteWidthType(fd, buf, count, 0, 0);

}


int catAnetBlockReadTime(int fd, char *buf, int count, int waitMs) {
    return anetReadWidthType(fd, buf, count, 2, waitMs);

}

int catAnetBlockWriteTime(int fd, char *buf, int count, int waitMs) {

    return anetWriteWidthType(fd, buf, count, 2, waitMs);
}


static int anetListen(char *err, int s, struct sockaddr *sa, socklen_t len, int backlog) {
    if (bind(s, sa, len) == -1) {
        anetSetError(err, "bind: %s", strerror(errno));
        close(s);
        return ANET_ERR;
    }

    if (listen(s, backlog) == -1) {
        anetSetError(err, "listen: %s", strerror(errno));
        close(s);
        return ANET_ERR;
    }
    return ANET_OK;
}

static int anetV6Only(char *err, int s) {
    return ANET_ERR;
//     int yes = 1;
//     if (setsockopt(s,IPPROTO_IPV6,IPV6_V6ONLY,&yes,sizeof(yes)) == -1) {
//         anetSetError(err, "setsockopt: %s", strerror(errno));
//         close(s);
//         return ANET_ERR;
//     }
//     return ANET_OK;
}

static int _anetTcpServer(char *err, int port, char *bindaddr, int af, int backlog) {
    int s, rv;
    char _port[6];  /* strlen("65535") */
    struct addrinfo hints, *servinfo, *p;

    snprintf(_port, 6, "%d", port);
    memset(&hints, 0, sizeof(hints));
    hints.ai_family = af;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_PASSIVE;    /* No effect if bindaddr != NULL */

    if ((rv = getaddrinfo(bindaddr, _port, &hints, &servinfo)) != 0) {
        anetSetError(err, "%s", gai_strerror(rv));
        return ANET_ERR;
    }
    for (p = servinfo; p != NULL; p = p->ai_next) {
        if ((s = socket(p->ai_family, p->ai_socktype, p->ai_protocol)) == -1)
            continue;

        //if (af == AF_INET6 && anetV6Only(err,s) == ANET_ERR) goto error;
        if (anetSetReuseAddr(err, s) == ANET_ERR) goto error;
        if (anetListen(err, s, p->ai_addr, p->ai_addrlen, backlog) == ANET_ERR) goto error;
        goto end;
    }
    if (p == NULL) {
        anetSetError(err, "unable to bind socket");
        goto error;
    }

    error:
    s = ANET_ERR;
    end:
    freeaddrinfo(servinfo);
    return s;
}

int catAnetTcpServer(char *err, int port, char *bindaddr, int backlog) {
    return _anetTcpServer(err, port, bindaddr, AF_INET, backlog);
}

int catAnetTcp6Server(char *err, int port, char *bindaddr, int backlog) {
    return _anetTcpServer(err, port, bindaddr, AF_INET6, backlog);
}

#ifndef WIN32

int catAnetUnixServer(char *err, char *path, mode_t perm, int backlog) {
    int s = ANET_ERR;
    struct sockaddr_un sa;

    if ((s = anetCreateSocket(err, AF_LOCAL)) == ANET_ERR)
        return ANET_ERR;

    memset(&sa, 0, sizeof(sa));
    sa.sun_family = AF_LOCAL;
    strncpy(sa.sun_path, path, sizeof(sa.sun_path) - 1);
    if (anetListen(err, s, (struct sockaddr *) &sa, sizeof(sa), backlog) == ANET_ERR)
        return ANET_ERR;
    if (perm)
        chmod(sa.sun_path, perm);


    return s;
}

#endif

static int anetGenericAccept(char *err, int s, struct sockaddr *sa, socklen_t *len) {
    int fd;
    while (1) {
        fd = accept(s, sa, len);
        if (fd == -1) {
            if (errno == EINTR)
                continue;
            else {
                anetSetError(err, "accept: %s", strerror(errno));
                return ANET_ERR;
            }
        }
        break;
    }
    return fd;
}

int catAnetTcpAccept(char *err, int s, char *ip, size_t ip_len, int *port) {
    int fd;
    struct sockaddr_storage sa;
    socklen_t salen = sizeof(sa);
    if ((fd = anetGenericAccept(err, s, (struct sockaddr *) &sa, &salen)) == -1)
        return ANET_ERR;

    if (sa.ss_family == AF_INET) {
        struct sockaddr_in *s = (struct sockaddr_in *) &sa;
        if (ip) inet_ntop(AF_INET, (void *) &(s->sin_addr), ip, ip_len);
        if (port) *port = ntohs(s->sin_port);
    } else {
        struct sockaddr_in6 *s = (struct sockaddr_in6 *) &sa;
        if (ip) inet_ntop(AF_INET6, (void *) &(s->sin6_addr), ip, ip_len);
        if (port) *port = ntohs(s->sin6_port);
    }
    return fd;
}

int catAnetUnixAccept(char *err, int s) {
    int fd = ANET_ERR;
#ifndef WIN32
    struct sockaddr_un sa;
    socklen_t salen = sizeof(sa);
    if ((fd = anetGenericAccept(err, s, (struct sockaddr *) &sa, &salen)) == -1)
        return ANET_ERR;

#endif

    return fd;
}

int catAnetPeerToString(int fd, char *ip, size_t ip_len, int *port) {
    struct sockaddr_storage sa;
    socklen_t salen = sizeof(sa);

    if (getpeername(fd, (struct sockaddr *) &sa, &salen) == -1) {
        if (port) *port = 0;
        ip[0] = '?';
        ip[1] = '\0';
        return -1;
    }
    if (sa.ss_family == AF_INET) {
        struct sockaddr_in *s = (struct sockaddr_in *) &sa;
        if (ip) inet_ntop(AF_INET, (void *) &(s->sin_addr), ip, ip_len);
        if (port) *port = ntohs(s->sin_port);
    } else {
        struct sockaddr_in6 *s = (struct sockaddr_in6 *) &sa;
        if (ip) inet_ntop(AF_INET6, (void *) &(s->sin6_addr), ip, ip_len);
        if (port) *port = ntohs(s->sin6_port);
    }
    return 0;
}

int catAnetSockName(int fd, char *ip, size_t ip_len, int *port) {
    struct sockaddr_storage sa;
    socklen_t salen = sizeof(sa);

    if (getsockname(fd, (struct sockaddr *) &sa, &salen) == -1) {
        if (port) *port = 0;
        ip[0] = '?';
        ip[1] = '\0';
        return -1;
    }
    if (sa.ss_family == AF_INET) {
        struct sockaddr_in *s = (struct sockaddr_in *) &sa;
        if (ip) inet_ntop(AF_INET, (void *) &(s->sin_addr), ip, ip_len);
        if (port) *port = ntohs(s->sin_port);
    } else {
        struct sockaddr_in6 *s = (struct sockaddr_in6 *) &sa;
        if (ip) inet_ntop(AF_INET6, (void *) &(s->sin6_addr), ip, ip_len);
        if (port) *port = ntohs(s->sin6_port);
    }
    return 0;
}

int catAnetClose(int fd) {
#ifdef WIN32
    return closesocket(fd);
#else
    close(fd);
#endif
    return 1;
}
