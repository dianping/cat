//
// Created by Terence on 2018/9/6.
//

#include <lib/cat_mpsc_queue.h>
#include <lib/cat_thread.h>

#define UT_MPSC_THREAD_NUM 32
#define UT_MPSC_PER_THREAD 5000
#define UT_MPSC_CAPACITY 10000

CatMPSCQueue *q;

PTHREAD produce(PVOID para) {
    for (int i = 0; i < UT_MPSC_PER_THREAD; i++) {
        auto * data = (int*) malloc(sizeof(int));
        *data = *(int*)para;
        if (CatMPSC_boffer(q, data, 1000) == 1) {
            printf("mis\n");
        }
    }
}

PTHREAD consume(PVOID para) {
    int buckets[100] = {0};
    int count = 0;

    while(1) {
        count++;
        void* data = CatMPSC_bpoll(q, 1000);
        if (nullptr == data) {
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
    printf("count: %d sum: %d size: %d\n", count, sum, CatMPSC_size(q));
}

int main() {
    q = newCatMPSCQueue("test", UT_MPSC_CAPACITY);

    pthread_t producers[UT_MPSC_THREAD_NUM];
    pthread_t consumer;

    pthread_create(&consumer, nullptr, consume, nullptr);
    for (int i = 0; i < UT_MPSC_THREAD_NUM; i++) {
        auto *para = (int*) malloc(sizeof(int));
        *para = i;
        pthread_create(&producers[i], nullptr, produce, para);
    }

    for (auto &producer : producers) {
        pthread_join(producer, nullptr);
    }
    pthread_join(consumer, nullptr);

    printf("capacity: %d\n", CatMPSC_capacity(q));
    printf("size: %d\n", CatMPSC_size(q));
}