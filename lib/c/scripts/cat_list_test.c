//
// Created by Terence on 2018/9/6.
//

#include <lib/cat_list.h>
#include <lib/cat_test.h>
#include <printf.h>

int main() {
    CatList *list = newCatList();
    ASSERT_NOT_NULL(list);

    for (int i = 0; i < 20; i++) {
        int *v = malloc(sizeof(int));
        *v = i;
        int r = list->push(list, (void*) v);
    }

    ASSERT_INT_EQ(20, list->size(list));

    CatListIterator* it = getCatListIterator(list);
    void* item;

    while (NULL != (item = it->next(it))) {
        printf("%d\n", *(int*)item);
    }
}
