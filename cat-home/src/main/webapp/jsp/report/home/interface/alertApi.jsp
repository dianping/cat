<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<h4 class="text-danger">告警Http API</h4>
<p>Cat支持其它系统通过调用HTTP API来发送信息。目前支持三种发送渠道：邮件、短信、微信（需要邮箱和“爱点评”微信订阅号绑定）</p>
<pre>
	http请求方式: GET或者POST
	http://主机域名:端口/cat/r/alert?
</pre>
<p>参数说明</p>
<table style="width:50%" class="table table-bordered table-striped table-condensed  ">
	<tr><th width="30%">参数</th><th width="70%">说明</th></tr>	
	<tr><td>op</td><td>执行操作<span class="text-danger">  必需[唯一值：alert]</span></td></tr>
	<tr><td>channel</td><td>渠道类型<span class="text-danger">  必需[可能值：mail, sms, weixin]</span></td></tr>
	<tr><td>title</td><td>告警标题<span class="text-danger">  必需</span></td></tr>
	<tr><td>content</td><td>告警内容<span class="text-danger">  短信可选，邮件、微信必需</span></td></tr>
	<tr><td>group</td><td>告警组名<span class="text-danger">  微信必需，短信、邮件可选</span></td></tr>	
	<tr><td>type</td><td>告警类型<span class="text-danger">  必需[可能值：network, business, exception, system, thirdParty, frontEndException]</span></td></tr>
	<tr><td>receivers</td><td>接收人<span class="text-danger">  必需[邮箱地址或者手机号；如有多个接收人，用半角逗号分割]</span></td></tr>
</table>

<p> url示例（get方式）</p>
<pre>
	http://cat.dianpingoa.com/cat/r/alert?op=alert&channel=mail&title=test&content=testcontent&group=cat&type=test&receivers=leon.li@dianping.com
</pre>
<p>返回说明</p>
<pre>
	<span class="text-success">{"status":200} ——> 成功</span>
	<span class="text-danger">{"status":500, "errorMessage":"lack receivers"} ——> 失败 [接收人receivers未填写或者格式错误]</span>
	<span class="text-danger">{"status":500, "errorMessage":"send failed, please check your channel argument"} ——> 失败 [渠道channel错误，请指定mail,sms,weixin三者中的一种渠道]</span>
	<span class="text-danger">{"status":500, "errorMessage":"send failed, please retry again"} ——> 失败 [发送异常]</span>
</pre>
</br>