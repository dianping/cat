# -*- coding: utf-8 -*-
# @Time    : 2018/1/27 上午11:23
# @Author  : 宜信致诚，徐岩华
# @File    : send_weixin_test.py
# @Software: PyCharm
import urllib2
import json

#
# weixin_token_dict = {
#     'access_token': 'FnXy6dPw4J8CuzRjCYvgWp1L2MWr9SJ2_XUkEELU736FI5aGvuNHbaL7n8Ab-9ZhYA2isbnHnTjx1k5TJCRppK7N74AEyYmnE3RvTd8he3jGrDFtU9dAGpMlHU94QnvSobXQY6uhbXcqSuAsvgCiJ9MjVnmaWC_8ntj9_D6rAo6dMzDd7_4gdE60eF-5wRD385GA_VVjmXun8b0qToNvEg'}
weixin_token_dict = {}


def send_weixin():
    '''
    1、send_weixin()函数以最简单的方式测试微信发送功能
    2、首先要去企业信息官网注册账号(https://work.weixin.qq.com)，注册后有详细的开发文档(https://work.weixin.qq.com/api/doc)
    3、除了注册企业微信之外，发送微信剩下的就是使用python的内建模块发送http请求了，下面两篇相关文章：
        http request：https://www.cnblogs.com/landhu/p/5104628.html
        json：https://www.cnblogs.com/kaituorensheng/p/3877382.html
    '''

    if len(weixin_token_dict) == 0:
        ID = '***'
        SECRECT = '***'
        response = urllib2.urlopen(
            'https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=' + ID + '&corpsecret=' + SECRECT)
        content = response.read()
        s = json.loads(content)
        weixin_token_dict['access_token'] = s['access_token']
        # 获取到token后，复制到weinxin_token_dict的value中，因为token有2个小时有的效期，且获取频率受限
        print 'new get token ->', weixin_token_dict['access_token']

    access_token = weixin_token_dict['access_token']

    send_url = 'https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=' + access_token
    params = {"touser": "user1|user2",
              "toparty": "",
              "totag": "",
              "msgtype": "text",
              "agentid": 1000001,
              "text": {
                  "content": "你的快递已到3，请携带工卡前往邮件中心领取。\n出发前可查看<a href=\"http://work.weixin.qq.com\">邮件中心视频实况</a>，聪明避开排队。"
              },
              "safe": 0}
    data_dict = json.dumps(params)
    header_dict = {'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko',
                   "Content-Type": "application/json"}
    req = urllib2.Request(url=send_url, data=data_dict, headers=header_dict)
    res = urllib2.urlopen(req)
    send_res = res.read()

    send_res = json.loads(send_res)
    errcode = send_res['errcode']  # https://work.weixin.qq.com/api/doc#10649
    errmsg = send_res['errmsg']
    print errcode
    if errcode == 0:
        print 'send weixin ok ~'
    elif errcode == 42001 or errcode == 40014:
        print 'access_token expired.'
    else:
        print 'send weixin error , more info:', errmsg
        send_res


if __name__ == '__main__':
    send_weixin()
