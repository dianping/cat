# -*- coding: utf-8 -*-
# @Time    : 2018/1/27 上午11:23
# @Author  : 宜信致诚，徐岩华
# @File    : main.py
# @Software: PyCharm

import BaseHTTPServer
import urllib
import sys
import logging
import sender

logging.basicConfig(level=logging.INFO,
                    format='[%(asctime)s] [%(levelname)s] %(filename)s[line:%(lineno)d] %(message)s',
                    datefmt='%Y-%m-%d %H:%M:%S',
                    filename='debug.log',
                    filemode='w')


def start_server(port):
    '''
    cat告警服务端
    :param port:服务监听端口
    :return:
    '''
    http_server = BaseHTTPServer.HTTPServer(("", port), MyHttpHandler)
    logging.info('----------------------------------------------')
    logging.info('cat-alert Server startup and listen on %d.', port)
    logging.info('----------------------------------------------')
    http_server.serve_forever()


class MyHttpHandler(BaseHTTPServer.BaseHTTPRequestHandler):

    def do_POST(self):
        url = self.path
        logging.info('receive request,url:%s', url)

        datas = self.rfile.read(int(self.headers['content-length']))
        params = trans_dicts(datas)
        sender_obj = 0
        if url == '/mail/':
            sender_obj = sender.sender_mail(params)
        elif url == '/sms/':
            sender_obj = sender.sender_sms(params)
        elif url == '/weixin/':
            sender_obj = sender.sender_weixin(params)
        else:
            logging.error("unsupport alert channel :%s", url)

        if sender_obj != 0:
            sender_obj.send()

        # for key in params:
        #     print key, '-->', params[key]
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write('200')


def trans_dicts(params):
    dicts = {}
    if len(params) == 0:
        return
    params = params.replace('+', '%20')
    params = params.split('&')
    for param in params:
        idx = param.find('=')
        key = param[:idx]
        value = urllib.unquote(param[idx + 1:]).decode("utf-8", "ignore")
        dicts[key] = value
    return dicts


if __name__ == '__main__':
    port = 8888
    if len(sys.argv) > 1 and sys.argv[1]:
        port = int(sys.argv[1])
    start_server(port)
