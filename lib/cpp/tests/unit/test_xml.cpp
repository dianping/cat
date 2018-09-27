//
// Created by Terence on 2018/8/15.
//

#include <stdio.h>
#include <gtest/gtest.h>

#include "lib/cat_ezxml.h"

#define DEFAULT_XML_FILE "cat/client.xml"

TEST(EZXML, read) {
    ezxml_t f1 = ezxml_parse_file(DEFAULT_XML_FILE), servers, server;
    ASSERT_NE(f1, nullptr);

    int r = 0;
    for (servers = ezxml_child(f1, "servers"); servers; servers = servers->next) {
        for (server = ezxml_child(servers, "server"); server; server = server->next) {
            r++;
            ASSERT_STREQ(ezxml_attr(server, "ip"), "127.0.0.1");
            ASSERT_STREQ(ezxml_attr(server, "port"), "2280");
            ASSERT_STREQ(ezxml_attr(server, "http-port"), "8080");
        }
    }

    ASSERT_GT(r, 0);
    ezxml_free(f1);
}

TEST(EZXML, write) {
    ASSERT_EQ(1, 1);
}
