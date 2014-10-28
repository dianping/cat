<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-error">APP命令字添加/删除API&nbsp;&nbsp;&nbsp;&nbsp; </h4>
<p>Cat支持其它系统通过调用HTTP API来添加、删除APP命令字</p>
<pre>
	http请求方式: GET或者POST
	http://主机域名:端口/cat/r/app?
</pre>
<p>参数说明</p>
<table style="width:70%" class="table table-striped table-bordered table-condensed">
	<tr><th width="30%">参数</th><th width="70%">说明</th></tr>	
	<tr><td>op</td><td>执行操作<span class="text-error">  必需[增加Command:appAdd 删除Command:appDelete]</span></td></tr>
	<tr><td>name</td><td>命令字名称<span class="text-error">  必需</span></td></tr>
	<tr><td>domain</td><td>所属项目<span class="text-error">  可选</span></td></tr>
	<tr><td>title</td><td>命令字标题<span class="text-error">  可选[建议添加，便于查看]</span></td></tr>
</table>
<p> url示例（get方式）</p>
<pre>
	http://cat.dianpingoa.com/cat/r/app?op=appAdd&domain=testDomain&name=testName&title=testTitle
	http://cat.dianpingoa.com/cat/r/app?op=appDelete&domain=testDomain&name=testName&title=testTitle
</pre>
<p>返回说明</p>
<pre>
	<span class="text-success">{"status":200} ——> 成功</span>
	<span class="text-error">{"status":500} ——> 失败</span>
	<span class="text-error">{"status":500, "info":"name is required."} ——> 失败 [缺少name参数]</span>
</pre>

<br/>
<h4 class="text-error">APP监控配置查询API&nbsp;&nbsp;&nbsp;&nbsp; </h4>
<p>Cat支持通过curl等方式获取APP监控配置（XML或JSON）</p>
<pre>
	http请求方式: GET或POST
	http://主机域名:端口/cat/s/config?
</pre>
<p>参数说明</p>
<table style="width:70%" class="table table-striped table-bordered table-condensed">
	<tr><th width="30%">参数</th><th width="70%">说明</th></tr>	
	<tr><td>op</td><td>执行操作<span class="text-error">  必需[为appConfigFetch]</span></td></tr>
	<tr><td>type</td><td>配置格式<span class="text-error">  可选[默认为JSON]</span></td></tr>
</table>
<p> url示例（get方式）</p>
<pre>
	http://cat.dianpingoa.com/cat/r/app?op=appConfigFetch&type=json
	http://cat.dianpingoa.com/cat/r/app?op=appConfigFetch&type=xml
</pre>
<p>返回说明</p>
<pre>
	<span class="text-success">配置文件数据 ——> 成功</span>
	<span class="text-error">空或其他错误 ——> 失败</span>
</pre>
<p class="text-error">注意，在浏览器中查看XML格式数据会显示为空，应该在代码中进行数据的读取（需要设置cookie）。例如：</p>
<pre>
	curl -b "ct=%E5%AD%99%E4%BD%B3%E6%9E%97|jialin.sun|1414490474065|10.1.1.109|***" -d "type=xml" http://cat.dp/cat/s/config?op=appConfigFetch
</pre>
	
<br/>

