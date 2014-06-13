<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h3 class="text-error">用户端监控文档</h3>
<h5 class="text-info"> a).从用户端角度来看点评的业务接口状态，这是一个端到端的监控，能最早的发现用户端出现问题，比如根本访问不到点评，某城市延迟很大等。</h5>
<h5 class="text-info"> b).用户端的监控目前能监控Ajax接口，页面Page不能监控到。</h5>
<h5 class="text-info"> c).一般一个应用会监控1-2个重要接口，后端实时分析会按照城市、运营商维度做一些聚合分析。</h5>

</br>
<h4>外部监控API文档</h4> 
<p>用途：提供外部监控的Http接口，用于监控用户端的错误信息。</p>
<p>1、为了保留以后的扩展性，移动端和Web端的暂定用不同的API接口。</p>
<p>2、公网IP，已经提交申请，后续我补充下。</p>
<p class="text-error">3、【电信，暂使用此IP】CTC:  114.80.165.63，文档后面{ip}使用这个。</p>
<p>4、【网通，暂时不使用】CNC:  140.207.217.23</p>

</br>
<h4 class="text-success">Web单次接口</h4>
	<pre>	http://{ip}/broker-service/api/single</pre>
	
	<p>参数说明</p>
	<table style="width:70%" class="table table-striped table-bordered table-condensed">
		<tr><th>query名</th><th>实际名称</th><th>描述</th><th>类型</th></tr>	
		<tr><td>v</td><td>version</td><td>API版本号</td><td>暂定为1</td></tr>
		<tr><td>ts</td><td>timestamp</td><td>发生时间</td><td>long型，1970到现在的毫秒数</td></tr>
		<tr><td>tu</td><td>targetUrl</td><td>调用的URL或API</td><td>String</td></tr>
		<tr><td>d</td><td>duration</td><td>访问耗时</td><td>long 毫秒</td></tr>
		<tr><td>hs</td><td>httpStatus</td><td>httpStatus</td><td>String</td></tr>
		<tr><td>ec</td><td>errorCode</td><td>ErrorCode</td><td>String，如果没有的话，传空串</td></tr>
	</table>

自定义 HttpStatus 表
	<table style="width:70%" class="table table-striped table-bordered table-condensed">
		<tr><th>code名称</th><th>code含义</th></tr>	
		<tr><td>-100</td><td>如果当前没有连接，不能连接到网络</td></tr>
		<tr><td>-107</td><td>回传的数据格式出错</td></tr>
		<tr><td>-901</td><td>当数据发送之后，500ms 之内没有收到 header</td></tr>
		<tr><td>-902</td><td>当收到header 和 httpstatus 之后，500ms 之内没有开始下载</td></tr>
		<tr><td>-903</td><td>当开始 loading 之后，500ms 之后仍然没有传送完毕</td></tr>
		<tr><td>-904</td><td>实际上不会出现这个 code，因为 readyState 置 4 之后就成功了</td></tr>
		<tr><td>-905</td><td>响应体的类型不符，比如 JSON.parse 失败</td></tr>
		<tr><td>-910</td><td>业务超时，当业务代码中设置了 timeout 以后，触发了超时</td></tr>
		<tr><td>-911</td><td>当业务代码中触发了 cancel 方法后，触发的 ajax 取消，有可能由业务逻辑所致，而不是错误。</td></tr>
		<tr><td>>0</td><td>业务 code</td></tr>
	</table>
</br>

<p>ec 参数</p>
<p>今后 ec 参数仅仅是用来标识 response json 中的 code 值</p>

</br>

<h4 class="text-success">Mobile批量接口</h4>
	<pre>	http://${ip}/broker-service/api/batch</pre>
	<p>批量接口POST内容，前面加上v=1&c=，不同请求之间用回车<span class="text-error">ENTER</span>分隔，字段之间用<span class="text-error">TAB</span>分隔。</p>
	<pre>
	单个请求格式如下:
	timstamp<span class="text-error">TAB</span>targetUrl<span class="text-error">TAB</span>duration<span class="text-error">TAB</span>httpCode<span class="text-error">TAB</span>errorCode<span class="text-error">ENTER</span>
	</pre>
	<p>POST内容如果有如下5个请求，Sample的POST内容为</p>
	<pre>
	v=1&c=
	1400037748182<span class="text-error">TAB</span>http://dianping.com/shop<span class="text-error">TAB</span>300<span class="text-error">TAB</span>200<span class="text-error">TAB</span>300<span class="text-error">ENTER</span>
	1400037748182<span class="text-error">TAB</span>http://dianping.com/shop<span class="text-error">TAB</span>300<span class="text-error">TAB</span>200<span class="text-error">TAB</span>300<span class="text-error">ENTER</span>
	1400037748182<span class="text-error">TAB</span>http://dianping.com/shop<span class="text-error">TAB</span>300<span class="text-error">TAB</span>200<span class="text-error">TAB</span>300<span class="text-error">ENTER</span>
	1400037748182<span class="text-error">TAB</span>http://dianping.com/shop<span class="text-error">TAB</span>300<span class="text-error">TAB</span>200<span class="text-error">TAB</span>300<span class="text-error">ENTER</span>
	1400037748182<span class="text-error">TAB</span>http://dianping.com/shop<span class="text-error">TAB</span>300<span class="text-error">TAB</span>200<span class="text-error">TAB</span>300<span class="text-error">ENTER</span>
	</pre>	
</br>

<h4 class="text-success">JS 错误接口</h4>
	<pre>	http://{ip}/broker-service/api/js</pre>
	
	<p>参数说明</p>
	<table style="width:70%" class="table table-striped table-bordered table-condensed">
		<tr><th>query名</th><th>实际名称</th><th>描述</th><th>类型</th></tr>	
		<tr><td>v</td><td>version</td><td>API版本号</td><td>暂定为1</td></tr>
		<tr><td>timestamp</td><td>timestamp</td><td>发生时间</td><td>long型，1970到现在的毫秒数</td></tr>
		<tr><td>error</td><td>error</td><td>错误的类型</td><td>String</td></tr>
		<tr><td>file</td><td>file</td><td>错误的发生的html页面</td><td>String</td></tr>
		<tr><td>line</td><td>line</td><td>错误的行数</td><td>int</td></tr>
		<tr><td>data</td><td>data</td><td>data类型</td><td>String，如果没有的话，传空串</td></tr>
	</table>
	
	
	</br>
<h4 class="text-success">CDN监控接口</h4>
	<pre>	http://{ip}/broker-service/api/cdn</pre>
	
	批量接口POST内容，前面加上v=1&c=，不同请求之间用回车<span class="text-error">ENTER</span>分隔，字段之间用<span class="text-error">TAB</span>分隔。

	<pre>
	timstamp<span class="text-error">TAB</span>targetUrl<span class="text-error">TAB</span>dnslookup<span class="text-error">TAB</span>tcpconnect<span class="text-error">TAB</span>request<span class="text-error">TAB</span>response<span class="text-error">ENTER</span>
	
	sample如下:
	
	v=1&c=
	1400037748182<span class="text-error">TAB</span>http://dianping.com/shop<span class="text-error">TAB</span>300<span class="text-error">TAB</span>200<span class="text-error">TAB</span>300<span class="text-error">TAB</span>300<span class="text-error">ENTER</span>
	1400037748182<span class="text-error">TAB</span>http://dianping.com/shop<span class="text-error">TAB</span>300<span class="text-error">TAB</span>200<span class="text-error">TAB</span>300<span class="text-error">TAB</span>300<span class="text-error">ENTER</span>
	1400037748182<span class="text-error">TAB</span>http://dianping.com/shop<span class="text-error">TAB</span>300<span class="text-error">TAB</span>200<span class="text-error">TAB</span>300<span class="text-error">TAB</span>300<span class="text-error">ENTER</span>
	</pre>
	
	<p>参数说明</p>
	<table style="width:70%" class="table table-striped table-bordered table-condensed">
		<tr><th>query名</th><th>实际名称</th><th>描述</th><th>类型</th></tr>	
		<tr><td>v</td><td>version</td><td>API版本号</td><td>暂定为1</td></tr>
		<tr><td>t</td><td>timestamp</td><td>发生时间</td><td>long型，1970到现在的毫秒数</td></tr>
		<tr><td>dl</td><td>dnslookup</td><td>dns寻址时间</td><td>int</td></tr>
		<tr><td>tc</td><td>tcpConnect</td><td>tcp连接建立</td><td>int</td></tr>
		<tr><td>rq</td><td>request</td><td>请求时间</td><td>int</td></tr>
		<tr><td>rs</td><td>response</td><td>接受时间</td><td>int</td></tr>
	</table>
	
<br/>
<h4 class="text-success">URL规则配置&nbsp;  <a target="_blank" href="/cat/s/config?op=urlPatternUpdate">链接</a></h4>

<table style="width:70%" class="table table-striped table-bordered table-condensed">
	<tr><th>ID</th><th>描述</th></tr>
	<tr><td>唯一ID</td><td>不能有特殊字符，仅限于英文字母和-</td></tr>	
	<tr><td>所属组</td><td>分析时不起作用，仅仅用作url的分组，用于展示目的</td></tr>	
	<tr><td>Pattern名</td><td>支持完全匹配方式，比如http://m.api.dianping.com/searchshop.api， 
部分匹配，比如 http://www.dianping.com/{City}/food，{City}可以匹配任何字符串</td></tr>	
</table>
<br/>
<h4 class="text-success">用户端监控报表&nbsp;  <a  target="_blank" href="/cat/r/userMonitor?domain=&ip=&date=&reportType=&op=urlPatternUpdate">链接</a></h4>

	
	
	