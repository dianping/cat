<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<h4>HTTP API调用方式</h4>
<br>
<h5 class="text-success"><strong>CAT接口调用请求说明</strong></h5>
<pre>
	http请求方式: GET（请使用http协议）
	http://cat.dianpingoa.com/cat/r/monitor?
</pre>
<p>参数说明</p>
<table style="width:90%" class="table table-bordered table-striped table-condensed">
	<tr><th width="30%">参数</th><th>说明</th></tr>
	<tr><td>group</td><td>监控组唯一ID名称，<span class="text-danger">必需，仅仅包括字母、数字，不能包含特殊字符，比如引号，冒号等。建议TuanGou这类命名方式</span></td></tr>
	<tr><td>domain</td><td>应用唯一ID名称，<span class="text-danger">必需，仅仅包括字母、数字，不能包含特殊字符，比如引号，冒号等。建议用TuanGouWeb这类命名方式</span></td></tr>
	<tr><td>key</td><td>监控业务唯一ID名称，<span class="text-danger">必需，仅仅包括字母、数字，不能包含特殊字符，比如引号，冒号等。建议用PayCount这类命名方式</span></td></tr>
	<tr><td>timestamp</td><td>时间戳,<span class="text-danger">必需，仅仅为数字。如果缺失，选取服务器当前时间</span></td></tr>
	<tr><td>op</td><td>sum，avg，count[<span class="text-danger">默认</span>]</td></tr>
	<tr><td>count</td><td>op=count时所需，<span class="text-danger">默认为1</span></td></tr>
	<tr><td>sum</td><td>op=sum时所需，<span class="text-danger">默认为0</span></td></tr>
	<tr><td>avg</td><td>op=avg时所需，<span class="text-danger">默认为0</span></td></tr>
</table>

<p> 1).op = count时，用于记录一个指标值出现的次数</p>
<pre>
	http://cat.dianpingoa.com/cat/r/monitor?timestamp=1404815988&group=myGroup&domain=myApp&key=myKey&op=count
</pre>
<p> 2).op = avg时，用于记录一个指标出现的平均值</p>
<pre>
	http://cat.dianpingoa.com/cat/r/monitor?timestamp=1404815988&group=myGroup&domain=myApp&key=myKey&op=avg&avg=500
</pre>
<p> 3).op = sum时，用于记录一个指标出现的总和</p>
<pre>
	http://cat.dianpingoa.com/cat/r/monitor?timestamp=1404815988&group=myGroup&domain=myApp&key=myKey&op=sum&sum=500
</pre>
<p> 4).op = batch时，用于批量提交指标。（TAB、ENTER分别是制表符和换行符）</p>
<pre>
	http://cat.dianpingoa.com/cat/r/monitor?op=batch&batch=group<span class="text-danger">TAB</span>domain<span class="text-danger">TAB</span>key<span class="text-danger">TAB</span>type<span class="text-danger">TAB</span>time<span class="text-danger">TAB</span>value<span class="text-danger">ENTER</span>
	group<span class="text-danger">TAB</span>domain<span class="text-danger">TAB</span>key<span class="text-danger">TAB</span>type<span class="text-danger">TAB</span>time<span class="text-danger">TAB</span>value<span class="text-danger">ENTER</span>
</pre>
<p>返回说明</p>
<pre>
	<span class="text-danger">{"statusCode":"-1","errorMsg":"Unknown [ domain,group,key ] name!"} ——> 失败 [必需参数缺失]</span>
	<span class="text-success">{"statusCode":"0"} ——> 成功</span>
</pre>
<br>