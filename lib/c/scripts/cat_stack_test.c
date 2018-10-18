//
// Created by Terence on 2018/9/6.
//

#include <lib/cat_stack.h>
#include <lib/cat_test.h>

#define CAPACITY 10

int main() {
    CatStack *s = newCatStack(CAPACITY);
    ASSERT_NOT_NULL(s);

    void* data = s->peek(s);
    ASSERT_NULL(data);

    for (int i = 0; i < CAPACITY + 5; i++) {
        int *v = malloc(sizeof(int));
        *v = i;
        int r = s->push(s, (void*) v);

        if (i < CAPACITY) {
            ASSERT_INT_EQ(r, CAT_STACK_PUSH_SUCCESS);
        } else {
            ASSERT_INT_NE(r, CAT_STACK_PUSH_SUCCESS);
        }
    }

    ASSERT_INT_EQ(CAPACITY, s->size(s));
    ASSERT_INT_EQ(CAPACITY, s->capacity(s));

    for (int i = CAPACITY; i > 0; i--) {
        void* item;
        item= s->peek(s);
        ASSERT_INT_EQ(i - 1, *(int*)item);
        item = s->pop(s);
        int a = *(int*)item;
        ASSERT_INT_EQ(i - 1, *(int*)item);
    }

    ASSERT_NULL(s->peek(s));
    ASSERT_NULL(s->pop(s));
    ASSERT_INT_EQ(0, s->size(s));
    ASSERT_INT_EQ(CAPACITY, s->capacity(s));

    deleteCatStack(s);
}