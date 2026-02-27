/* Hash Tables Implementation - Copyright (C) 2006-2008 Salvatore Sanfilippo
 * antirez at gmail dot com
 *
 * Please see dict.c for more information
 */

#ifndef __DICT_H
#define __DICT_H

#ifdef __cplusplus
extern "C" {
#endif

#define DICT_OK 0
#define DICT_ERR 1

/* Unused arguments generate annoying warnings... */
#define DICT_NOTUSED(V) ((void) V)

typedef struct dictEntry {
    void *key;
    void *val;
    struct dictEntry *next;
} dictEntry;

typedef struct dictType {
    unsigned int (*hashFunction)(const void *key);

    void *(*keyDup)(void *privdata, const void *key);

    void *(*valDup)(void *privdata, const void *obj);

    int (*keyCompare)(void *privdata, const void *key1, const void *key2);

    void (*keyDestructor)(void *privdata, void *key);

    void (*valDestructor)(void *privdata, void *obj);
} dictType;

typedef struct dict {
    dictEntry **table;
    dictType *type;
    unsigned int size;
    unsigned int sizemask;
    unsigned int used;
    void *privdata;
} dict;

typedef struct dictIterator {
    dict *ht;
    int index;
    dictEntry *entry, *nextEntry;
} dictIterator;

/* This is the initial size of every hash table */
#define DICT_HT_INITIAL_SIZE     16

/* ------------------------------- Macros ------------------------------------*/
#define dictFreeEntryVal(ht, entry) \
    if ((ht)->type->valDestructor) \
        (ht)->type->valDestructor((ht)->privdata, (entry)->val)

#define dictSetHashVal(ht, entry, _val_) do { \
    if ((ht)->type->valDup) \
        entry->val = (ht)->type->valDup((ht)->privdata, _val_); \
    else \
        entry->val = (_val_); \
} while(0)

#define dictFreeEntryKey(ht, entry) \
    if ((ht)->type->keyDestructor) \
        (ht)->type->keyDestructor((ht)->privdata, (entry)->key)

#define dictSetHashKey(ht, entry, _key_) do { \
    if ((ht)->type->keyDup) \
        entry->key = (ht)->type->keyDup((ht)->privdata, _key_); \
    else \
        entry->key = (_key_); \
} while(0)

#define dictCompareHashKeys(ht, key1, key2) \
    (((ht)->type->keyCompare) ? \
        (ht)->type->keyCompare((ht)->privdata, key1, key2) : \
        (key1) == (key2))

#define dictHashKey(ht, key) (ht)->type->hashFunction(key)

#define dictGetEntryKey(he) ((he)->key)
#define dictGetEntryVal(he) ((he)->val)
#define dictGetHashTableSize(ht) ((ht)->size)
#define dictGetHashTableUsed(ht) ((ht)->used)

#ifdef __cplusplus
unsigned int _dictStringCopyHTHashFunction(const void *key);

void *_dictStringCopyHTKeyDup(void *privdata, const void *key);

void *_dictStringKeyValCopyHTValDup(void *privdata, const void *val);

int _dictStringCopyHTKeyCompare(void *privdata, const void *key1,
                                       const void *key2);

void _dictStringCopyHTKeyDestructor(void *privdata, void *key);
#endif

/* API */
dict *catDictCreate(dictType *type, void *privDataPtr);

int catDictExpand(dict *ht, unsigned int size);

int catDictAdd(dict *ht, void *key, void *val);

int catDictReplace(dict *ht, void *key, void *val);

int catDictDelete(dict *ht, const void *key);

int catDictDeleteNoFree(dict *ht, const void *key);

void catDictRelease(dict *ht);

dictEntry *catDictFind(dict *ht, const void *key);

int catDictResize(dict *ht);

dictIterator *catDictGetIterator(dict *ht);

dictEntry *catDictNext(dictIterator *iter);

void catDictReleaseIterator(dictIterator *iter);

dictEntry *catDictGetRandomKey(dict *ht);

void catDictPrintStats(dict *ht);

unsigned int catDictGenHashFunction(const unsigned char *buf, int len);

void catDictEmpty(dict *ht);

/* Hash table types */
extern dictType dictTypeHeapStringCopyKey;
extern dictType dictTypeHeapStrings;
extern dictType dictTypeHeapStringCopyKeyValue;


#ifdef __cplusplus
}
#endif

#endif /* __DICT_H */
