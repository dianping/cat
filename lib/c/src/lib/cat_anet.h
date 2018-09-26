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

#ifndef ANET_H
#define ANET_H

#define ANET_OK 0
#define ANET_ERR -1
#define ANET_ERR_LEN 256

/* Flags used with certain functions. */
#define ANET_NONE 0
#define ANET_IP_ONLY (1<<0)

#if defined(__sun) || defined(_AIX)
#define AF_LOCAL AF_UNIX
#endif

#ifdef _AIX
#undef ip_len
#endif

int catAnetTcpConnect(char *err, char *addr, int port);

int catAnetTcpNonBlockConnect(char *err, char *addr, int port);

int catAnetUnixConnect(char *err, char *path);

int catAnetUnixNonBlockConnect(char *err, char *path);

int catAnetGetHost(char *err, char *host, size_t ipbuf_len);

int catAnetResolve(char *err, char *host, char *ipbuf, size_t ipbuf_len);

int catAnetResolveIP(char *err, char *host, char *ipbuf, size_t ipbuf_len);

int catAnetResolveIPHex(char *err, char *host, char *ipbuf, size_t ipbuf_len);

int catAnetTcpServer(char *err, int port, char *bindaddr, int backlog);

int catAnetTcp6Server(char *err, int port, char *bindaddr, int backlog);

#ifndef WIN32

int catAnetUnixServer(char *err, char *path, mode_t perm, int backlog);

#endif

int catAnetTcpAccept(char *err, int serversock, char *ip, size_t ip_len, int *port);

int catAnetUnixAccept(char *err, int serversock);

int catAnetBlockRead(int fd, char *buf, int count);

int catAnetBlockWrite(int fd, char *buf, int count);

int catAnetBlockReadTime(int fd, char *buf, int count, int waitMs);

int catAnetBlockWriteTime(int fd, char *buf, int count, int waitMs);


int catAnetNoBlockRead(int fd, char *buf, int count);

int catAnetNoBlockWrite(int fd, char *buf, int count);

int catAnetNonBlock(char *err, int fd);

int catAnetBlock(char *err, int fd);

int catAnetEnableTcpNoDelay(char *err, int fd);

int catAnetDisableTcpNoDelay(char *err, int fd);

int catAnetTcpKeepAlive(char *err, int fd);

int catAnetSendTimeout(char *err, int fd, long long ms);

int catAnetPeerToString(int fd, char *ip, size_t ip_len, int *port);

int catAnetKeepAlive(char *err, int fd, int interval);

int catAnetSockName(int fd, char *ip, size_t ip_len, int *port);

int catAnetClose(int fd);


#endif
