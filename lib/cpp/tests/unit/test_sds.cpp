//
// Created by Terence on 2018/8/14.
//

#include <gtest/gtest.h>

#include "lib/cat_sds.h"

TEST(SDS, test) {
    sds s = catsdsnew("abc");
    ASSERT_EQ(catsdslen(s), 3);
    ASSERT_STREQ(s, "abc");
}