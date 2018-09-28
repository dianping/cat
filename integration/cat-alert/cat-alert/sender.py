# -*- coding: utf-8 -*-
# @Time    : 2018/1/27 上午11:23
# @Author  : 宜信致诚，徐岩华
# @File    : sender.py
# @Software: PyCharm

__metaclass__ = type
from email.mime.text import MIMEText
from email.header import Header
import smtplib
import logging
import urllib2
import urllib
import json
from string import Template
import re


# 消息发送接口
class sender:
    logging.basicConfig(level=logging.INFO,
                        format='[%(asctime)s] [%(levelname)s] %(filename)s[line:%(lineno)d] %(message)s',
                        datefmt='%Y-%m-%d %H:%M:%S',
                        filename='debug.log',
                        filemode='w')

    def __init__(self, params):
        self.params = params

    def send(self):
        pass


# 消息发送接口
class sender_mail(sender):

    def __init__(self, params):
        super(sender_mail, self).__init__(params)

    def send(self):
        """
        username --> ***@sina.com
        from_addr --> ***@sina.com
        smtp_server --> smtp.sina.com
        content --> [CAT 第三方告警] [项目: ] : [[type=get  details=HTTP URL[1234568888888888.com?] GET访问出现异常]][时间: 2015-01-15 18:20]
        <a href='http://cat/r/p?domain=&date=2015011518'>点击此处查看详情</a>
        to_addrs --> ***@yourcompany.com
        password --> ****
        type --> 1500
        subject --> [CAT第三方告警] [项目: ]
        """
        mail_from = self.params.get('from_addr')
        mail_to = self.params.get('to_addrs')
        subject = self.params.get('subject')
        smtp_server = self.params.get('smtp_server')
        content = self.params.get('content')
        username = self.params.get('username')
        password = self.params.get('password')

        if not mail_from or not mail_to or not smtp_server or not username:
            logging.error('mail require info is null,title is %s', subject)
            return
        try:
            msg = MIMEText(content, 'html', 'utf-8')
            msg['Subject'] = Header(subject, 'utf-8')
            msg['from'] = mail_from
            msg['to'] = mail_to

            smtp = smtplib.SMTP()
            smtp.connect(smtp_server)
            smtp.login(username, password)
            to_list = mail_to.split(',')
            smtp.sendmail(mail_from, to_list, msg.as_string())
            smtp.quit()
            logging.info('mail send ok ~. title is %s.', subject)
        except Exception, e:
            logging.error('mail send exception -> %s', e.message)


# 消息发送接口
class sender_sms(sender):

    def __init__(self, params):
        self.params = params

    def send(self):
        logging.warn('channel[sms] is not supported!')


#这个字典用以缓存access_token
weixin_token_dict = {
    'access_token': 'zszwVW_VgAIqhu_QZZQOzgjPZmOft2p5OZcCZMmvR97oYHMxjMS0JuBgmGLZxjo8tBT7tWodSIXKxPwgO8iCBVvGGJimOGpSG3C8kzj1pB5WAKnm6tRbsVUc1V-nijwZO0azh1fO4nlE52UGq0iEOoArf3dPJ5drK23gwFyZE8cYLBXwR6dOk8WblP4tvXxwuSm4FVe2F6_F1gJUiGxtkg'}


# 微信发送
class sender_weixin(sender):

    def __init__(self, params):
        super(sender_weixin, self).__init__(params)
        # CAT告警应用参数
        self.weixin_dict = {'CorpID': '****',
                            'AgentId': '1000001',
                            'Secret': '****'}
        url_template_gettoken = 'https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=${CorpID}&corpsecret=${Secret}'
        self.url_gettoken = Template(url_template_gettoken).substitute(self.weixin_dict)

    def get_token(self):
        if len(weixin_token_dict) == 0:
            response = urllib2.urlopen(self.url_gettoken)
            content = response.read()
            s = json.loads(content)
            access_token = s['access_token']
            weixin_token_dict['access_token'] = access_token
            logging.info('new get token -> %s', access_token)
        else:
            access_token = weixin_token_dict['access_token']
        return access_token

    def send(self):
        logging.info("weixin-send：%s", self.params)

        title = self.params.get('title')
        content = self.params.get('content')
        weixins = self.params.get('weixins')

        if not title or not content or not weixins:
            logging.error('weixin require info is null,title is %s', title)
            return
        try:
            weixins = weixins.replace(',', '|')
            logging.info('weixin-content--->%s', content)
            matchObj = re.findall(r'\[\s*(.*?)\s*\]', content, re.M)
            msg = "\n".join(matchObj).replace(u'异常数量', u'\n异常数量')

            access_token = self.get_token()
            url_template_send = 'https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=${access_token}'
            send_url = Template(url_template_send).substitute({'access_token': access_token})
            params = {"touser": weixins, "toparty": "", "totag": "", "msgtype": "text",
                      "agentid": self.weixin_dict['AgentId'], "text": {"content": msg}, "safe": 0}
            data_dict = json.dumps(params)
            header_dict = {'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko',
                           "Content-Type": "application/json"}
            req = urllib2.Request(url=send_url, data=data_dict, headers=header_dict)
            res = urllib2.urlopen(req)
            send_res = res.read()
            send_res = json.loads(send_res)
            errcode = send_res['errcode']  # https://work.weixin.qq.com/api/doc#10649
            errmsg = send_res['errmsg']
            if errcode == 0:
                logging.info('weixin send ok ~.title is %s', title)
            elif errcode == 42001 or errcode == 40014:
                logging.error('access_token expired.')
                weixin_token_dict.clear()
                self.send()
            else:
                logging.error('weixin send error , more info->errcode:%d,errmsg:%s', errcode, errmsg)
        except Exception, e:
            logging.error('weixin send exception -> %s', e.message)
