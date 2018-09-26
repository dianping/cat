#ifndef CAT_CLIENT_C_STATIC_QUEUE_H
#define CAT_CLIENT_C_STATIC_QUEUE_H

#ifdef __cplusplus
extern "C" {
#endif

#include <string.h>
#include <stdlib.h>
#include <stddef.h>

#ifdef WIN32
#define inline __inline
#endif

typedef struct _CATStaticQueue {
    size_t maxQueueSize;
    volatile int head;
    volatile int tail;
    volatile size_t size;
    void *valueArray[];
} CATStaticQueue;

#define CATSTATICQUEUE_ERR -1
#define CATSTATICQUEUE_OK 0

CATStaticQueue *createCATStaticQueue(size_t maxQueueSize);

int pushBackCATStaticQueue(CATStaticQueue *pQueue, void *pData);

int pushFrontCATStaticQueue(CATStaticQueue *pQueue, void *pData);

void *popBackCATStaticQueue(CATStaticQueue *pQueue);

void *popFrontCATStaticQueue(CATStaticQueue *pQueue);

void *pryBackCATStaticQueue(CATStaticQueue *pQueue);

void *pryFrontCATStaticQueue(CATStaticQueue *pQueue);

void *getCATStaticQueueByIndex(CATStaticQueue *pQueue, size_t index);

void clearCATStaticQueue(CATStaticQueue *pQueue);

void destroyCATStaticQueue(CATStaticQueue *pQueue);

static inline size_t getCATStaticQueueSize(CATStaticQueue *pQueue) {
    return pQueue->size;
}

static inline int isCATStaticQueueEmpty(CATStaticQueue *pQueue) {
    return !pQueue->size;
}

static inline int isCATStaticQueueFull(CATStaticQueue *pQueue) {
    return !(pQueue->maxQueueSize - pQueue->size);
}

static inline int getCATStaticQueueRightDirect(CATStaticQueue *pQueue) {
    return pQueue->head - pQueue->tail;
}

typedef CATStaticQueue CATStaticStack;

#define createCATStaticStack createCATStaticQueue
#define pushCATStaticStack pushFrontCATStaticQueue
#define popCATStaticStack popFrontCATStaticQueue
#define peekCATStaticStack pryFrontCATStaticQueue
#define getCATStaticStackSize getCATStaticQueueSize
#define isCATStaticStackEmpty isCATStaticQueueEmpty
#define isCATStaticStackFull isCATStaticQueueFull
#define getCATStaticStackByIndex getCATStaticQueueByIndex
#define clearCATStaticStack clearCATStaticQueue
#define destroyCATStaticStack destroyCATStaticQueue

typedef CATStaticQueue CATStaticFIFOQueue;

#define createCATStaticFIFOQueue createCATStaticQueue
#define pushCATStaticFIFOQueue pushBackCATStaticQueue
#define popCATStaticFIFOQueue popFrontCATStaticQueue
#define peekCATStaticFIFOQueue pryFrontCATStaticQueue
#define getCATStaticFIFOQueueSize getCATStaticQueueSize
#define isCATStaticFIFOQueueEmpty isCATStaticQueueEmpty
#define isCATStaticFIFOQueueFull isCATStaticQueueFull
#define getCATStaticFIFOQueueByIndex getCATStaticQueueByIndex
#define clearCATStaticFIFOQueue clearCATStaticQueue
#define destroyCATStaticFIFOQueue destroyCATStaticQueue

#ifdef __cplusplus
}
#endif

#endif //CAT_CLIENT_C_STATIC_QUEUE_H
