//
// Created by Terence on 2018/9/6.
//

#include <lib/cat_mpsc_queue.h>
#include <lib/cat_thread.h>
#include <lib/cat_time_util.h>

#define UT_MPSC_THREAD_NUM 100
#define UT_MPSC_PER_THREAD 10000
#define UT_MPSC_CAPACITY 10000

CatMPSCQueue *q;

pthread_t producers[UT_MPSC_THREAD_NUM];
pthread_t consumer;

ATOMICLONG produced_count = 0;

PTHREAD produce(PVOID para) {
    for (int i = 0; i < UT_MPSC_PER_THREAD; i++) {
        int* data = malloc(sizeof(int));
        *data = *(int*)para;
        if (CatMPSC_boffer(q, data, 1000) == 1) {
            printf("mis\n");
        }
        ATOMICLONG_INC(&produced_count);
    }
}

PTHREAD consume(PVOID para) {
    int buckets[100] = {0};
    int count = 0;

    while(1) {
        count++;
        void* data = CatMPSC_bpoll(q, 1000);
        if (NULL == data) {
            break;
        }
        int tid = *(int*)data;
        buckets[tid]++;
        free(data);
    }

    int sum = 0;
    for(int i = 0; i < UT_MPSC_THREAD_NUM; i++) {
        sum += buckets[i];
        printf("%d: %d\n", i, buckets[i]);
    }
    printf("produced: %ld count: %d sum: %d size: %d\n", produced_count, count, sum, CatMPSC_size(q));
}

int main() {
    q = newCatMPSCQueue("test", UT_MPSC_CAPACITY);

    for (int i = 0; i < UT_MPSC_THREAD_NUM; i++) {
        int *para = malloc(sizeof(int));
        *para = i;
        pthread_create(&producers[i], NULL, produce, para);
    }

    Sleep(100);
    pthread_create(&consumer, NULL, consume, NULL);

    for (int i = 0; i < UT_MPSC_THREAD_NUM; i++) {
        pthread_join(producers[i], NULL);
    }
    pthread_join(consumer, NULL);

    printf("capacity: %d\n", CatMPSC_capacity(q));
    printf("size: %d\n", CatMPSC_size(q));
}