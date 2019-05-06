/* SDSLib, A C dynamic strings library
 *
 * Copyright (c) 2006-2010, Salvatore Sanfilippo <antirez at gmail dot com>
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

#ifndef CCAT_CAT_SDS_H
#define CCAT_CAT_SDS_H

#define SDS_MAX_PREALLOC (1024*1024)

#include <sys/types.h>
#include <stdarg.h>

#ifdef WIN32
#define inline __inline
#endif

#if defined(__cplusplus) || defined(c_plusplus)
extern "C" {
#endif

typedef char *sds;

typedef struct _sdshdr {
    unsigned int len;
    unsigned int free;
    char buf[];
} sdshdr;

static inline size_t catsdslen(const sds s) {
    sdshdr *sh = (sdshdr *) (s - (sizeof(sdshdr)));
    return sh->len;
}

static inline size_t catsdsavail(const sds s) {
    sdshdr *sh = (sdshdr *) (s - (sizeof(sdshdr)));
    return sh->free;
}

sds catsdsnewlen(const void *init, size_t initlen);

sds catsdsnewEmpty(size_t preAlloclen);

sds catsdsnew(const char *init);

sds catsdsempty(void);

size_t catsdslen(const sds s);

sds catsdsdup(const sds s);

void catsdsfree(sds s);

size_t catsdsavail(const sds s);

sds catsdsgrowzero(sds s, size_t len);

sds catsdscatlen(sds s, const void *t, size_t len);

sds catsdscat(sds s, const char *t);

sds catsdscatchar(sds s, char c);

sds catsdscatsds(sds s, const sds t);

sds catsdscpylen(sds s, const char *t, size_t len);

sds catsdscpy(sds s, const char *t);

sds catsdscatvprintf(sds s, const char *fmt, va_list ap);

#ifdef __GNUC__

sds catsdscatprintf(sds s, const char *fmt, ...)
__attribute__((format(printf, 2, 3)));

#else
sds catsdscatprintf(sds s, const char *fmt, ...);
#endif

sds catsdscatfmt(sds s, char const *fmt, ...);

sds catsdstrim(sds s, const char *cset);

void catsdsrange(sds s, int start, int end);

void catsdsupdatelen(sds s);

void catsdsclear(sds s);

int catsdscmp(const sds s1, const sds s2);

sds *catsdssplitlen(const char *s, int len, const char *sep, int seplen, int *count);

void catsdsfreesplitres(sds *tokens, int count);

void catsdstolower(sds s);

void catsdstoupper(sds s);

sds catsdsfromlonglong(long long value);

sds catsdscatrepr(sds s, const char *p, size_t len);

sds *catsdssplitargs(const char *line, int *argc);

sds catsdsmapchars(sds s, const char *from, const char *to, size_t setlen);

sds catsdsjoin(char **argv, int argc, char *sep);

/* Low level functions exposed to the user API */
sds catsdsMakeRoomFor(sds s, size_t addlen);

void catsdsIncrLen(sds s, int incr);

sds catsdsRemoveFreeSpace(sds s);

size_t catsdsAllocSize(sds s);

#ifdef __cplusplus
}
#endif

#endif //CCAT_CAT_SDS_H
