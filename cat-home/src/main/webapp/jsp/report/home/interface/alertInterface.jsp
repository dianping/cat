<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-danger">告警API&nbsp;&nbsp;&nbsp;&nbsp; </h4>
<p>Cat支持其它系统通过调用HTTP API来添加告警信息</p>
<pre>
	http请求方式: GET或者POST
	http://主机域名:端口/cat/r/alert?
</pre>
<p>参数说明</p>
<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
	<tr><th width="30%">参数</th><th width="70%">说明</th></tr>	
	<tr><td>op</td><td>执行操作<span class="text-danger">  必需[只能为insert]</span></td></tr>
	<tr><td>domain</td><td>告警项目名<span class="text-danger">  必需</span></td></tr>
	<tr><td>level</td><td>告警级别<span class="text-danger">  可选，默认为warning</span></td></tr>
	<tr><td>category</td><td>告警类型<span class="text-danger">  可选，默认为zabbix</span></td></tr>
	<tr><td>alertTime</td><td>告警时间<span class="text-danger">  可选，格式为yyyy-MM-dd HH:mm:ss(get方式需要转码),默认为插入时间</span></td></tr>
	<tr><td>metric</td><td>告警指标<span class="text-danger">  可选</span></td></tr>
	<tr><td>content</td><td>告警内容<span class="text-danger">  可选</span></td></tr>
</table>
<p> url示例（get方式）</p>
<pre>
	http://cat.dianpingoa.com/cat/r/alert?op=insert&domain=testDomain&level=warning&category=zabbix&metric=testMetric&content=testContent&alertTime=2014-01-01%2000:00:00
</pre>
<p>返回说明</p>
<pre>
	<span class="text-success">{"status":200} ——> 成功</span>
	<span class="text-danger">{"status":500} ——> 失败</span>
	<span class="text-danger">{"status":500, "errorMessage":"lack domain"} ——> 失败 [缺少domain参数]</span>
</pre>