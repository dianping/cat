<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-error">APP监控报表获取&nbsp;&nbsp;&nbsp;&nbsp; </h4>
<p>Cat支持其它系统通过调用HTTP API来获取APP监控报表数据（JSON格式）</p>
<pre>
	http请求方式: GET或者POST
	http://主机域名:端口/cat/r/app?
</pre>
<p>参数说明</p>
<table style="width:70%" class="table table-striped table-bordered table-condensed">
	<tr><th width="30%">参数</th><th width="70%">说明</th></tr>	
	<tr><td>op</td><td>linechartJson[查看API访问趋势、运营活动趋势]、piechartJson[查看访问量分布]<span class="text-error">  必需</span></td></tr>
	<tr><td>其他参数</td><td>参考端到端APP监控文档，除了op参数不同，其他均相同，可直接复用<span class="text-error">  必需</span></td></tr>
</table>
<p> url示例（<span class="text-error">红色部分为不同参数，没有op则需要添加。其他参数相同。</span>）</p>
<pre>
	http://cat.dianpingoa.com/cat/r/app?<span class="text-error">op=view</span>&query1=2014-10-28;1;;;;;;;;;&query2=&type=request&groupByField=&sort=&domains=default&commandId=1&domains2=default&commandId2=1&showActivity=false 为APP监控查看的URL链接
	则获取报表的URL为：
	http://cat.dianpingoa.com/cat/r/app?<span class="text-error">op=linechartJson&</span>query1=2014-10-28;1;;;;;;;;;&query2=&type=request&groupByField=&sort=&domains=default&commandId=1&domains2=default&commandId2=1&showActivity=false</pre>
<br>
<h4 class="text-error">WEB监控报表获取&nbsp;&nbsp;&nbsp;&nbsp; </h4>
<p>Cat支持其它系统通过调用HTTP API来获取WEB监控报表数据（JSON格式）</p>
<pre>
	http请求方式: GET或者POST
	http://主机域名:端口/cat/r/web?
</pre>
<p>参数说明</p>
<table style="width:70%" class="table table-striped table-bordered table-condensed">
	<tr><th width="30%">参数</th><th width="70%">说明</th></tr>	
	<tr><td>op</td><td>json<span class="text-error">  必需</span></td></tr>
	<tr><td>其他参数</td><td>参考端到端WEB监控文档，除了op参数不同，其他均相同，可直接复用<span class="text-error">  必需</span></td></tr>
</table>
<p> url示例（get方式），红色部分为不同参数，其他相同</p>
<pre>
	http://cat.dianpingoa.com/cat/r/web?<span class="text-error">op=view&</span>url=s1-small-dnsLookup&group=cdn-s1&city=上海市-&type=info&channel=&startDate=2014-10-28%2016:00&endDate=2014-10-28%2019:00 为APP监控查看的URL链接
	则获取报表的URL为：
	http://cat.dianpingoa.com/cat/r/web?<span class="text-error">op=json&</span>url=s1-small-dnsLookup&group=cdn-s1&city=上海市-&type=info&channel=&startDate=2014-10-28%2016:00&endDate=2014-10-28%2019:00
</pre>
