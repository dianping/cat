<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-danger">用户端监控文档</h4>
<h5 class="text-info"> a).从用户端角度来看点评的业务接口状态，这是一个端到端的监控，能最早的发现用户端出现问题，比如根本访问不到点评，某城市延迟很大等。</h5>
<h5 class="text-info"> b).用户端的监控目前能监控Ajax接口，页面Page不能监控到。</h5>
<h5 class="text-info"> c).一般一个应用会监控1-2个重要接口，后端实时分析会按照城市、运营商维度做一些聚合分析。</h5>

</br>
<h4>外部监控API文档</h4> 
<p>用途：提供外部监控的Http接口，用于监控用户端的错误信息。</p>
<p>1、为了保留以后的扩展性，移动端和Web端的暂定用不同的API接口。</p>
<p>2、公网IP，已经提交申请，后续我补充下。</p>
<p class="text-danger">3、【电信，暂使用此IP】CTC:  114.80.165.63，文档后面{ip}使用这个。</p>
<p>4、【网通，暂时不使用】CNC:  140.207.217.23</p>

</br>
<h4 class="text-danger">Web单次接口</h4>
	<pre>	http://{ip}/broker-service/api/single</pre>
	
	<p>参数说明</p>
	<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
		<tr><th>query名</th><th>实际名称</th><th>描述</th><th>类型</th></tr>	
		<tr><td>v</td><td>version</td><td>API版本号</td><td>暂定为1</td></tr>
		<tr><td>ts</td><td>timestamp</td><td>发生时间</td><td>long型，1970到现在的毫秒数</td></tr>
		<tr><td>tu</td><td>targetUrl</td><td>调用的URL或API</td><td>String</td></tr>
		<tr><td>d</td><td>duration</td><td>访问耗时</td><td>long 毫秒</td></tr>
		<tr><td>hs</td><td>httpStatus</td><td>httpStatus</td><td>整型</td></tr>
		<tr><td>ec</td><td>errorCode</td><td>ErrorCode</td><td>整型，如果没有的话，传空</td></tr>
	</table>

自定义 HttpStatus 表
	<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
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

<h4 class="text-danger">APP用户访问批量接口</h4>
	<pre>	http://{ip}/broker-service/api/batch</pre>
	<p>批量接口POST内容，前面加上“<span class="text-danger">v=2&c=</span>”(v=1已遗弃)，不同请求之间用回车<span class="text-danger">ENTER</span>分隔，字段之间用<span class="text-danger">TAB</span>分隔。</p>
	
	<table class="table table-bordered table-striped table-condensed  ">
		<tr><th>实际名称</th><th>描述</th><th>类型</th></tr>
		<tr><td>timestamp</td><td>发送数据时的时间戳</td><td>long</td></tr>
		<tr><td>network</td><td>2G,3G,4G,WIFI(iOS只有3G和WIFI)，1=wifi, 2=2G, 3=3G, 4=4G, 0=Unknown</td><td>int</td></tr>
		<tr><td>version</td><td>versionCode,比如6.8=680,只支持int类型</td><td>int</td></tr>
		<tr><td>tunnel</td><td>0 or 1，默认是0表示短连接，1表示是长连</td><td>int</td></tr>
		<tr><td>command</td><td>接口，一般为url path的最后一个单位(shop.bin)</td><td>String</td></tr>
		<tr><td>code</td><td>status code,建议区分http的返回码,比如>1000为业务错误码,<1000为网络错误码,<0为自定义错误码</td><td>int</td></tr>
		<tr><td>platform</td><td>android=1,ios=2,Unknown=0</td><td>int</td></tr>
		<tr><td>requestbyte</td><td>发送字节数</td><td>int</td></tr>
		<tr><td>responsebyte</td><td>返回字节数</td><td>int</td></tr>
		<tr><td>responsetime</td><td>用时 (毫秒）</td><td>int</td></tr>
	</table>
	
	<pre>
	单个请求格式如下
	timstamp<span class="text-danger">TAB</span>network<span class="text-danger">TAB</span>version<span class="text-danger">TAB</span>tunnel<span class="text-danger">TAB</span>command<span class="text-danger">TAB</span>code<span class="text-danger">TAB</span>platform<span class="text-danger">TAB</span>requestbyte<span class="text-danger">TAB</span>responsebyte<span class="text-danger">TAB</span>responsetime<span class="text-danger">ENTER</span>
	
	</pre>
	<p>POST内容如果有如下5个请求，Sample的POST内容为，</p>
	<p class="text-danger">v=2&c=不需要做urlencode，后面的批量的content部分需要urlencode。</p>
	<pre>
	v=2&c=
	1400037748152<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>1<span class="text-danger">\t</span>100<span class="text-danger">\t</span>100<span class="text-danger">\t</span>200<span class="text-danger">\n</span> 
	1400037748163<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>2<span class="text-danger">\t</span>120<span class="text-danger">\t</span>110<span class="text-danger">\t</span>300<span class="text-danger">\n</span> 
	1400037748174<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>3<span class="text-danger">\t</span>110<span class="text-danger">\t</span>120<span class="text-danger">\t</span>200<span class="text-danger">\n</span> 
	1400037748185<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>1<span class="text-danger">\t</span>120<span class="text-danger">\t</span>130<span class="text-danger">\t</span>100<span class="text-danger">\n</span> 
	1400037748196<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>500<span class="text-danger">\t</span>2<span class="text-danger">\t</span>110<span class="text-danger">\t</span>140<span class="text-danger">\t</span>200<span class="text-danger">\n</span>
	</pre>	
</br>

<h4 class="text-danger">APP加载速度批量接口</h4>
	<pre>	http://{ip}/broker-service/api/speed</pre>
	<p>批量接口POST内容，前面加上“<span class="text-danger">v=1&c=</span>”，不同请求之间用回车<span class="text-danger">ENTER</span>分隔，字段之间用<span class="text-danger">TAB</span>分隔。</p>
	
	<table class="table table-bordered table-striped table-condensed  ">
		<tr><th>实际名称</th><th>描述</th><th>类型</th></tr>
		<tr><td>timestamp</td><td>发送数据时的时间戳</td><td>long</td></tr>
		<tr><td>network</td><td>2G,3G,4G,WIFI (iOS只有3G和WIFI)</td><td>int</td></tr>
		<tr><td>version</td><td>versionCode, eg. 6.8 = 680</td><td>int</td></tr>
		<tr><td>platform</td><td>android=1 or ios=2</td><td>int</td></tr>
		<tr><td>page</td><td>加载页面，eg. index.bin</td><td>String</td></tr>
		<tr><td>step1-responseTime1</td><td>页面加载第1阶段及延时，eg. 1-300</td><td>String,responseTime单位为毫秒</td></tr>
		<tr><td>step2-responseTime2</td><td>页面加载第2阶段及延时，eg. 1-300</td><td>String,responseTime单位为毫秒</td></tr>
		<tr><td>.......</td><td>页面加载阶段及延时，eg. 1-300</td><td>String,responseTime单位为毫秒</td></tr>
		<tr><td>stepN-responseTimeN</td><td>页面加载第N阶段及延时，eg. 1-300</td><td>String,responseTime单位为毫秒</td></tr>
	</table>
	
	<pre>
	单个请求格式如下:
	timstamp<span class="text-danger">TAB</span>network<span class="text-danger">TAB</span>version<span class="text-danger">TAB</span>platform<span class="text-danger">TAB</span>page<span class="text-danger">TAB</span>step1-responseTime1<span class="text-danger">TAB</span>step2-responseTime2<span class="text-danger">TAB</span>step3-responseTime3<span class="text-danger">ENTER</span>
	
	</pre>
	<p>POST内容如果有如下5个请求，Sample的POST内容为</p>
	<pre>
	v=1&c=
	1400037748152<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>page1<span class="text-danger">\t</span>1-20<span class="text-danger">\t</span>2-30<span class="text-danger">\t</span>3-40<span class="text-danger">\t</span>4-50<span class="text-danger">\n</span> 
	1400037748163<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>page2<span class="text-danger">\t</span>1-20<span class="text-danger">\t</span>2-30<span class="text-danger">\t</span>3-40<span class="text-danger">\t</span>4-50<span class="text-danger">\n</span> 
	1400037748174<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>pgae3<span class="text-danger">\t</span>1-20<span class="text-danger">\t</span>2-30<span class="text-danger">\t</span>3-40<span class="text-danger">\t</span>4-50<span class="text-danger">\n</span> 
	1400037748185<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>page4<span class="text-danger">\t</span>1-20<span class="text-danger">\t</span>2-30<span class="text-danger">\t</span>3-40<span class="text-danger">\t</span>4-50<span class="text-danger">\n</span> 
	1400037748196<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>page5<span class="text-danger">\t</span>1-20<span class="text-danger">\t</span>2-30<span class="text-danger">\t</span>3-40<span class="text-danger">\t</span>4-50<span class="text-danger">\n</span>
	</pre>	
</br>

<h4 class="text-danger">APP Crash日志接口</h4>
	<pre>	http://{ip}/broker-service/api/crash</pre>
	<table class="table table-bordered table-striped table-condensed  ">
		<tr><th>参数名</th><th>描述</th><th>类型</th></tr>
		<tr><td>mt</td><td>手机类型，andriod传入1，ios传入2</td><td>int</td></tr>
		<tr><td>av</td><td>APP的版本号，比如1.0.0</td><td>String</td></tr>
		<tr><td>pv</td><td>平台版本，比如7.0.1</td><td>String</td></tr>
		<tr><td>m</td><td>模块名，支持模块区分</td><td>String</td></tr>
		<tr><td>msg</td><td>crash的简单原因，后续统计根据msg进行分类，比如NullPointException</td><td>String</td></tr>
		<tr><td>l</td><td>错误等级，默认值可以传warning、error可以用来进行错误区分</td><td>String</td></tr>
		<tr><td>d</td><td>详细的错误日志</td><td>String</td></tr>
	</table>
	
	<p class="text-danger">参数可以post上来，需要对value进行encode。</p>
	<p class="text-danger">如下手机类型是ios，app版本号1.1，平台版本号1.2，模块是user，错误等级为error，错误原因为java.npe</p>
	<pre>
		http://{ip}/broker-service/api/crash?mt=2&av=1.1&pv=1.2&m=user&msg=java.npe&l=error&d=dddddsfsdfsdfsdf	
	</pre>
</br>


<h4 class="text-danger">APP 长连访问批量接口</h4>
	<pre>	http://{ip}/broker-service/api/connection</pre>
	<p>批量接口POST内容，前面加上“<span class="text-danger">v=3&c=</span>”，不同请求之间用回车<span class="text-danger">ENTER</span>分隔，字段之间用<span class="text-danger">TAB</span>分隔。</p>
	
	<table class="table table-bordered table-striped table-condensed  ">
		<tr><th>实际名称</th><th>描述</th><th>类型</th></tr>
		<tr><td>timestamp</td><td>发送数据时的时间戳</td><td>long</td></tr>
		<tr><td>network</td><td>2G,3G,4G,WIFI(iOS只有3G和WIFI)，1=wifi, 2=2G, 3=3G, 4=4G, 0=Unknown</td><td>int</td></tr>
		<tr><td>version</td><td>versionCode,比如6.8=680,只支持int类型</td><td>int</td></tr>
		<tr><td>tunnel</td><td>固定为1，表示是长连</td><td>int</td></tr>
		<tr><td>command</td><td>接口，一般为url path的最后一个单位(shop.bin)</td><td>String</td></tr>
		<tr><td>code</td><td>status code,建议区分http的返回码,比如>1000为业务错误码,<1000为网络错误码,<0为自定义错误码</td><td>int</td></tr>
		<tr><td>platform</td><td>android=1,ios=2,Unknown=0</td><td>int</td></tr>
		<tr><td>requestbyte</td><td>发送字节数</td><td>int</td></tr>
		<tr><td>responsebyte</td><td>返回字节数</td><td>int</td></tr>
		<tr><td>responsetime</td><td>用时 (毫秒）</td><td>int</td></tr>
	</table>
	
	<pre>
	单个请求格式如下
	timstamp<span class="text-danger">TAB</span>network<span class="text-danger">TAB</span>version<span class="text-danger">TAB</span>tunnel<span class="text-danger">TAB</span>command<span class="text-danger">TAB</span>code<span class="text-danger">TAB</span>platform<span class="text-danger">TAB</span>requestbyte<span class="text-danger">TAB</span>responsebyte<span class="text-danger">TAB</span>responsetime<span class="text-danger">ENTER</span>
	
	新版本加入了接入点IP
	</pre>
	<p>POST内容如果有如下5个请求，Sample的POST内容为，</p>
	<p class="text-danger">v=2&c=不需要做urlencode，后面的批量的content部分需要urlencode。</p>
	<pre>
	v=3&c=
	1400037748152<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>1<span class="text-danger">\t</span>100<span class="text-danger">\t</span>100<span class="text-danger">\t</span>200<span class="text-danger">\n</span> 
	1400037748163<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>2<span class="text-danger">\t</span>120<span class="text-danger">\t</span>110<span class="text-danger">\t</span>300<span class="text-danger">\n</span> 
	1400037748174<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>3<span class="text-danger">\t</span>110<span class="text-danger">\t</span>120<span class="text-danger">\t</span>200<span class="text-danger">\n</span> 
	1400037748185<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>1<span class="text-danger">\t</span>120<span class="text-danger">\t</span>130<span class="text-danger">\t</span>100<span class="text-danger">\n</span> 
	1400037748196<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>500<span class="text-danger">\t</span>2<span class="text-danger">\t</span>110<span class="text-danger">\t</span>140<span class="text-danger">\t</span>200<span class="text-danger">\n</span>
	</pre>	
</br>

<h4 class="text-danger">JS 错误接口</h4>
	<pre>	http://{ip}/broker-service/api/js</pre>
	
	<p>参数说明</p>
	<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
		<tr><th>query名</th><th>实际名称</th><th>描述</th><th>类型</th></tr>	
		<tr><td>v</td><td>version</td><td>API版本号</td><td>暂定为1</td></tr>
		<tr><td>timestamp</td><td>timestamp</td><td>发生时间</td><td>long型，1970到现在的毫秒数</td></tr>
		<tr><td>error</td><td>error</td><td>错误的类型</td><td>String</td></tr>
		<tr><td>file</td><td>file</td><td>错误的发生的js文件</td><td>String</td></tr>
		<tr><td>url</td><td>url</td><td>错误的发生的html页面</td><td>String</td></tr>
		<tr><td>line</td><td>line</td><td>错误的行数</td><td>int</td></tr>
		<tr><td>data</td><td>data</td><td>data类型</td><td>String，如果没有的话，传空串</td></tr>
	</table>
	</br>
<h4 class="text-danger">CDN监控接口</h4>
	<pre>	http://{ip}/broker-service/api/cdn</pre>
	
	批量接口POST内容，前面加上v=1&c=，不同请求之间用回车<span class="text-danger">ENTER</span>分隔，字段之间用<span class="text-danger">TAB</span>分隔。

	<pre>
	timstamp<span class="text-danger">TAB</span>targetUrl<span class="text-danger">TAB</span>dnslookup<span class="text-danger">TAB</span>tcpconnect<span class="text-danger">TAB</span>request<span class="text-danger">TAB</span>response<span class="text-danger">ENTER</span>
	
	sample如下:
	
	v=1&c=
	1400037748182<span class="text-danger">TAB</span>cdn-resource1<span class="text-danger">TAB</span>300<span class="text-danger">TAB</span>200<span class="text-danger">TAB</span>300<span class="text-danger">TAB</span>300<span class="text-danger">ENTER</span>
	1400037748182<span class="text-danger">TAB</span>cdn-resource2<span class="text-danger">TAB</span>300<span class="text-danger">TAB</span>200<span class="text-danger">TAB</span>300<span class="text-danger">TAB</span>300<span class="text-danger">ENTER</span>
	1400037748182<span class="text-danger">TAB</span>cdn-resource3<span class="text-danger">TAB</span>300<span class="text-danger">TAB</span>200<span class="text-danger">TAB</span>300<span class="text-danger">TAB</span>300<span class="text-danger">ENTER</span>
	</pre>
	
	<p>参数说明</p>
	<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
		<tr><th>实际名称</th><th>描述</th><th>类型</th></tr>	
		<tr><td>v</td><td>API版本号</td><td>暂定为1</td></tr>
		<tr><td>c</td><td>具体内容</td><td>content内容</td></tr>
	</table>
	<p>content内容说明</p>
	<pre>
	timstamp<span class="text-danger">TAB</span>targetUrl<span class="text-danger">TAB</span>dnslookup<span class="text-danger">TAB</span>tcpconnect<span class="text-danger">TAB</span>request<span class="text-danger">TAB</span>response<span class="text-danger">ENTER</span>
	</pre>
	<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
		<tr><th>实际名称</th><th>描述</th><th>类型</th></tr>	
		<tr><td>timestamp</td><td>发生时间</td><td>long型，1970到现在的毫秒数</td></tr>
		<tr><td>targetUrl</td><td>具体的cdn资源</td><td>cdn资源的一个定义</td></tr>
		<tr><td>dnslookup</td><td>dns寻址时间</td><td>int</td></tr>
		<tr><td>tcpConnect</td><td>tcp连接建立</td><td>int</td></tr>
		<tr><td>request</td><td>请求时间</td><td>int</td></tr>
		<tr><td>response</td><td>接受时间</td><td>int</td></tr>
	</table>
<br/>
<h4 class="text-success">URL规则配置&nbsp;  <a target="_blank" href="/cat/s/config?op=urlPatternUpdate">链接</a></h4>

<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
	<tr><th>ID</th><th>描述</th></tr>
	<tr><td>唯一ID</td><td>不能有特殊字符，仅限于英文字母和-</td></tr>	
	<tr><td>所属组</td><td>分析时不起作用，仅仅用作url的分组，用于展示目的</td></tr>	
	<tr><td>Pattern名</td><td>支持完全匹配方式，比如http://m.api.dianping.com/searchshop.api， 
部分匹配，比如 http://www.dianping.com/{City}/food，{City}可以匹配任何字符串</td></tr>	
</table>
<br/>
<h4 class="text-danger">APP监控报表获取&nbsp;&nbsp;&nbsp;&nbsp; </h4>
<p>Cat支持其它系统通过调用HTTP API来获取APP监控报表数据（JSON格式）</p>
<pre>
	http请求方式: GET或者POST
	http://主机域名:端口/cat/r/app?
</pre>
<p>参数说明</p>
<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
	<tr><th width="30%">参数</th><th width="70%">说明</th></tr>	
	<tr><td>op</td><td>linechartJson[查看API访问趋势、运营活动趋势]、piechartJson[查看访问量分布]<span class="text-danger">  必需</span></td></tr>
	<tr><td>其他参数</td><td>参考端到端APP监控文档，除了op参数不同，其他均相同，可直接复用<span class="text-danger">  必需</span></td></tr>
</table>
<p> url示例<span class="text-danger">（红色部分为不同参数，没有op则需要添加，其他参数相同）</span></p>
<pre>
	http://cat.dianpingoa.com/cat/r/app?<span class="text-danger">op=view</span>&query1=2014-10-28;1;;;;;;;;;&query2=&type=request&groupByField=&sort=&domains=default&commandId=1&domains2=default&commandId2=1 为APP监控查看的URL链接
	则获取报表的URL为：
	http://cat.dianpingoa.com/cat/r/app?<span class="text-danger">op=linechartJson&</span>query1=2014-10-28;1;;;;;;;;;&query2=&type=request&groupByField=&sort=&domains=default&commandId=1&domains2=default&commandId2=1</pre>
<br>
<h4 class="text-danger">WEB监控报表获取&nbsp;&nbsp;&nbsp;&nbsp; </h4>
<p>Cat支持其它系统通过调用HTTP API来获取WEB监控报表数据（JSON格式）</p>
<pre>
	http请求方式: GET或者POST
	http://主机域名:端口/cat/r/web?
</pre>
<p>参数说明</p>
<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
	<tr><th width="30%">参数</th><th width="70%">说明</th></tr>	
	<tr><td>op</td><td>json<span class="text-danger">  必需</span></td></tr>
	<tr><td>其他参数</td><td>参考端到端WEB监控文档，除了op参数不同，其他均相同，可直接复用<span class="text-danger">  必需</span></td></tr>
</table>
<p> url示例<span class="text-danger">（红色部分为不同参数，没有op则需要添加，其他参数相同）</span></p>
<pre>
	http://cat.dianpingoa.com/cat/r/web?<span class="text-danger">op=view&</span>url=s1-small-dnsLookup&group=cdn-s1&city=上海市-&type=info&channel=&startDate=2014-10-28%2016:00&endDate=2014-10-28%2019:00 为APP监控查看的URL链接
	则获取报表的URL为：
	http://cat.dianpingoa.com/cat/r/web?<span class="text-danger">op=json&</span>url=s1-small-dnsLookup&group=cdn-s1&city=上海市-&type=info&channel=&startDate=2014-10-28%2016:00&endDate=2014-10-28%2019:00
</pre>



	
	
	