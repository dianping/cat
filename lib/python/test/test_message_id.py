#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: litong ()
# Email: <litong@litonglitong.com>

import cat
import time

def test3():
    try:
        trans = cat.Transaction("Trans", "T3")

        message_id = cat.create_message_id()
        print(message_id)
        remote_server_message_id = cat.create_remote_server_message_id("test.cat.com")
        print(remote_server_message_id)

        print(cat.get_thread_local_message_tree_id())
        print(cat.get_thread_local_message_tree_parent_id())
        print(cat.get_thread_local_message_tree_root_id())


        cat.set_thread_local_message_tree_id(message_id)
        print(cat.get_thread_local_message_tree_id())

        cat.set_thread_local_message_tree_parent_id(cat.create_message_id())
        print(cat.get_thread_local_message_tree_parent_id())

        cat.set_thread_local_message_tree_root_id(cat.create_message_id())
        print(cat.get_thread_local_message_tree_root_id())

        trans.add_data("content")
        trans.add_data("key", "val")
        trans.set_status("error")
        trans.set_duration(500)
        trans.set_duration_start(time.time() * 1000 - 30 * 1000)
        trans.set_timestamp(time.time() * 1000 - 30 * 1000)
    finally:
        # NOTE don't forget to complete the transaction!
        trans.complete()


if __name__ == '__main__':

    cat.init("xx.cat.com", debug=True, logview=True)
    test3()

    time.sleep(1)

