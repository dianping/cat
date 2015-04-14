<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-danger">变更监控文档</h4>
<h4 class="text-info">记录及展示变更内容</h4>
<h4 class="text-success">HTTP API调用方式</h4>
<p>接口调用请求说明(插入数据)</p>
<pre>
	http请求方式: GET或者POST
	http://主机域名:端口/cat/r/alteration?
</pre>
<p>参数说明</p>
<table style="width:50%" class="table table-bordered table-striped table-condensed  ">
	<tr><th width="30%">参数</th><th width="70%">说明</th></tr>	
	<tr><td>op</td><td>执行操作<span class="text-danger">  必需[唯一值：insert]</span></td></tr>
	<tr><td>type</td><td>变更类型<span class="text-danger">  必需[可能值：puppet, workflow, lazyman]</span></td></tr>
	<tr><td>title</td><td>变更标题<span class="text-danger">  必需</span></td></tr>
	<tr><td>domain</td><td>变更项目<span class="text-danger">  必需</span></td></tr>
	<tr><td>hostname</td><td>变更机器域名<span class="text-danger">  必需</span></td></tr>	
	<tr><td>alterationDate</td><td>变更时间<span class="text-danger">  必需[格式如：2014-03-30 00:00:00]</span></td></tr>
	<tr><td>user</td><td>发起变更用户<span class="text-danger">  必需</span></td></tr>
	<tr><td>content</td><td>变更内容<span class="text-danger">  必需</span></td></tr>
	<tr><td>group</td><td>变更组别<span class="text-success">  可选</span></td></tr>
	<tr><td>ip</td><td>变更机器ip<span class="text-success">  可选</span></td></tr>
	<tr><td>url</td><td>变更连接<span class="text-success">  可选</span></td></tr>
</table>

<p> url示例（get方式）</p>
<pre>
	http://主机域名:端口/cat/r/alteration?op=insert&type=puppet&title=2&domain=3&hostname=1&alterationDate=2013-02-19%2000:00:00&user=5&content=6&group=&ip=&url=
</pre>
<p>返回说明</p>
<pre>
	<span class="text-success">{"status":200} ——> 成功</span>
	<span class="text-danger">{"status":500, "errorMessage":"lack args"} ——> 失败 [必需参数不全]</span>
	<span class="text-danger">{"status":500} ——> 失败 [其它错误]</span>
</pre>
</br>

