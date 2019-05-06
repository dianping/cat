//
// Created by Terence on 2018/10/18.
//

#ifndef CCAT_TEST_H
#define CCAT_TEST_H

static int g_assertions = 0;

static void inline _assert_null(void* ptr) {
    g_assertions++;
    if (NULL != ptr) {
        printf("The pointer expected to be null, non-null value given.\n");
    }
}

static void inline _assert_not_null(void* ptr) {
    g_assertions++;
    if (NULL == ptr) {
        printf("The pointer expected to be non-null value, null given.\n");
    }
}

static void inline _assert_int_eq(int expect, int actual) {
    g_assertions++;
    if (expect != actual) {
        printf ("The given value expected to be %d, %d given.\n", expect, actual);
    }
}

static void inline _assert_int_ne(int expect, int actual) {
    g_assertions++;
    if (expect == actual) {
        printf ("The given value expected to be not equal to %d.\n", expect);
    }
}

#define ASSERT_NULL(ptr) _assert_null(ptr)

#define ASSERT_NOT_NULL(ptr) _assert_not_null(ptr)

#define ASSERT_INT_EQ(expect, actual) _assert_int_eq(expect, actual)

#define ASSERT_INT_NE(expect, actual) _assert_int_ne(expect, actual)

#endif //CCAT_TEST_H
