<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-danger">用户端监控文档</h4>
<h5 class="text-info"> a).从用户端角度来看点评的业务接口状态，这是一个端到端的监控，能最早的发现用户端出现问题，比如根本访问不到点评，某城市延迟很大等。</h5>
<h5 class="text-info"> b).用户端的监控目前能监控Ajax接口，页面Page不能监控到。</h5>
<h5 class="text-info"> c).一般一个应用会监控1-2个重要接口，后端实时分析会按照城市、运营商维度做一些聚合分析。</h5>

<br/>
<h4>外部监控API文档</h4> 
<p>用途：提供外部监控的Http接口，用于监控用户端的错误信息。</p>
<p>1、为了保留以后的扩展性，移动端和Web端的暂定用不同的API接口。</p>
<p>2、公网IP，<span class="text-danger">114.80.165.63</span>，文档后面{ip}使用这个</p>
<p>2、公网IP，<span class="text-danger">http://catdot.dianping.com/</span>，也可以使用此域名</p>
<br/>

<h4 class="text-danger">APP用户访问批量上报接口</h4>
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
		<tr><td>ip</td><td>客户端连接的connection server的ip，长连接的代理ip</td><td>String</td></tr>
	</table>
	目前有三个version，v=2 , v=3 或者 v=4.
	目前在queryString，还有一个可选参数，是app的来源，参数用p代替，例如 p=1 , 目前点评主APP=1，团购APP=2
	
	<pre>
	单个请求格式如下
	timstamp<span class="text-danger">TAB</span>network<span class="text-danger">TAB</span>version<span class="text-danger">TAB</span>tunnel<span class="text-danger">TAB</span>command<span class="text-danger">TAB</span>code<span class="text-danger">TAB</span>platform<span class="text-danger">TAB</span>requestbyte<span class="text-danger">TAB</span>responsebyte<span class="text-danger">TAB</span>responsetime<span class="text-danger">ENTER</span>
	</pre>
	<p>POST内容如果有如下5个请求，Sample的POST内容为，</p>
	<p class="text-danger">v=2&c=不需要做urlencode，后面的批量的content部分需要urlencode。</p>
	<pre>
	v=2&p=1&c=
	1400037748152<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>1<span class="text-danger">\t</span>100<span class="text-danger">\t</span>100<span class="text-danger">\t</span>200<span class="text-danger">\n</span> 
	1400037748163<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>2<span class="text-danger">\t</span>120<span class="text-danger">\t</span>110<span class="text-danger">\t</span>300<span class="text-danger">\n</span> 
	1400037748174<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>3<span class="text-danger">\t</span>110<span class="text-danger">\t</span>120<span class="text-danger">\t</span>200<span class="text-danger">\n</span> 
	1400037748185<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>1<span class="text-danger">\t</span>120<span class="text-danger">\t</span>130<span class="text-danger">\t</span>100<span class="text-danger">\n</span> 
	1400037748196<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>500<span class="text-danger">\t</span>2<span class="text-danger">\t</span>110<span class="text-danger">\t</span>140<span class="text-danger">\t</span>200<span class="text-danger">\n</span>
	</pre>
	
	<br>
	
	v=3 在v=2的基础上，最后扩展了一个ip字段，表示当前连接的connection server的ip地址，暂时不做统计
	<pre>
	单个请求格式如下
	timstamp<span class="text-danger">TAB</span>network<span class="text-danger">TAB</span>version<span class="text-danger">TAB</span>tunnel<span class="text-danger">TAB</span>command<span class="text-danger">TAB</span>code<span class="text-danger">TAB</span>platform<span class="text-danger">TAB</span>requestbyte<span class="text-danger">TAB</span>responsebyte<span class="text-danger">TAB</span>responsetime<span class="text-danger">TAB</span>ip<span class="text-danger">ENTER</span>
	</pre>
	<p>POST内容如果有如下5个请求，Sample的POST内容为，</p>
	<p class="text-danger">v=3&c=不需要做urlencode，后面的批量的content部分需要urlencode。</p>
	<pre>
	v=3&p=1&c=
	1400037748152<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>1<span class="text-danger">\t</span>100<span class="text-danger">\t</span>100<span class="text-danger">\t</span>200<span class="text-danger">\t</span>10.1.6.128<span class="text-danger">\n</span> 
	1400037748163<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>2<span class="text-danger">\t</span>120<span class="text-danger">\t</span>110<span class="text-danger">\t</span>300<span class="text-danger">\t</span>10.1.6.128<span class="text-danger">\n</span> 
	1400037748174<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>3<span class="text-danger">\t</span>110<span class="text-danger">\t</span>120<span class="text-danger">\t</span>200<span class="text-danger">\t</span>10.1.6.128<span class="text-danger">\n</span> 
	1400037748185<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>1<span class="text-danger">\t</span>120<span class="text-danger">\t</span>130<span class="text-danger">\t</span>100<span class="text-danger">\t</span>10.1.6.128<span class="text-danger">\n</span> 
	1400037748196<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>500<span class="text-danger">\t</span>2<span class="text-danger">\t</span>110<span class="text-danger">\t</span>140<span class="text-danger">\t</span>200<span class="text-danger">\t</span>10.1.6.128<span class="text-danger">\n</span>
	</pre>	
	<br/>
	v=4 在v=3的基础上，最后扩展了一个customData字段，用于存放自定义字段，暂时不做统计
	<pre>
	单个请求格式如下
	timstamp<span class="text-danger">TAB</span>network<span class="text-danger">TAB</span>version<span class="text-danger">TAB</span>tunnel<span class="text-danger">TAB</span>command<span class="text-danger">TAB</span>code<span class="text-danger">TAB</span>platform<span class="text-danger">TAB</span>requestbyte<span class="text-danger">TAB</span>responsebyte<span class="text-danger">TAB</span>responsetime<span class="text-danger">TAB</span>ip<span class="text-danger">TAB</span>customData<span class="text-danger">ENTER</span>
	</pre>
	<p>POST内容如果有如下5个请求，Sample的POST内容为，</p>
	<p class="text-danger">v=4&c=不需要做urlencode，后面的批量的content部分需要urlencode。</p>
	<pre>
	v=3&p=1&c=
	1400037748152<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>1<span class="text-danger">\t</span>100<span class="text-danger">\t</span>100<span class="text-danger">\t</span>200<span class="text-danger">\t</span>10.1.6.128<span class="text-danger">\t</span>customData<span class="text-danger">\n</span> 
	1400037748163<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>2<span class="text-danger">\t</span>120<span class="text-danger">\t</span>110<span class="text-danger">\t</span>300<span class="text-danger">\t</span>10.1.6.128<span class="text-danger">\t</span>customData<span class="text-danger">\n</span> 
	1400037748174<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>3<span class="text-danger">\t</span>110<span class="text-danger">\t</span>120<span class="text-danger">\t</span>200<span class="text-danger">\t</span>10.1.6.128<span class="text-danger">\t</span>customData<span class="text-danger">\n</span> 
	1400037748185<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>200<span class="text-danger">\t</span>1<span class="text-danger">\t</span>120<span class="text-danger">\t</span>130<span class="text-danger">\t</span>100<span class="text-danger">\t</span>10.1.6.128<span class="text-danger">\t</span>customData<span class="text-danger">\n</span> 
	1400037748196<span class="text-danger">\t</span>1<span class="text-danger">\t</span>680<span class="text-danger">\t</span>1<span class="text-danger">\t</span>shop.bin<span class="text-danger">\t</span>500<span class="text-danger">\t</span>2<span class="text-danger">\t</span>110<span class="text-danger">\t</span>140<span class="text-danger">\t</span>200<span class="text-danger">\t</span>10.1.6.128<span class="text-danger">\t</span>customData<span class="text-danger">\n</span>
	</pre>	
	
<br/>

<h4 class="text-danger">APP加载速度批量上报接口</h4>
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
<br/>

<h4 class="text-danger">APP Crash日志上报接口</h4>
	<pre>
	beta: http://broker-service01.beta/broker-service/crashlog
	线上: http://catdot.dianping.com/broker-service/crashlog	</pre>
	 <h5>HTTP Body : </h5>
	<p class="text-danger">key-value形式，json格式传上来</p>
	<table class="table table-bordered table-striped table-condensed  ">
		<tr><th>参数名</th><th>描述</th></tr>
		<tr><td>appId</td><td>app在cat中的唯一ID，可在<a href="http://cat.dp/cat/s/app?op=appSources">APP来源</a>中查询或注册。</td></tr>
		<tr><td>appVersion</td><td>App版本号</td></tr>
		<tr><td>platVersion</td><td>系统版本号</td></tr>
		<tr><td>deviceBrand</td><td>设备品牌</td></tr>
		<tr><td>deviceModel</td><td>设备硬件型号</td></tr>
		<tr><td>crashTime</td><td>crash发生时间</td></tr>
		<tr><td>unionId</td><td>用于查询个案的id值</td></tr>
		<tr><td>platform</td><td>平台类型，分为android和ios</td></tr>
		<tr><td>reason</td><td>crash简要原因</td></tr>
		<tr><td>mapId</td><td>可为空，Android混淆map的id，ios不传</td></tr>
		<tr><td>crashContent</td><td>crash的详细内容</td></tr>
	</table>
	<pre>
	Http Body示例：
	{
		"crashContent":"xxxxx",
		"platform":"ios",
		"platVersion":"9.3.0",
		"crashTime":"2016-05-17 14:24:30",
		"appId":"1",
		"appVersion":"1.1.1",
		"reason":"test 123",
		"deviceBrand":"iphone",
		"deviceModel":"8,0",
		"unionId":"4765514784693486078"
	}
	</pre>
	
<br/>
 <h5>原有接口：</h5>
	<pre>	http://{ip}/broker-service/api/crash</pre>
		<p class="text-danger">参数可以post上来，需要对value进行encode。</p>
	<table class="table table-bordered table-striped table-condensed  ">
		<tr><th>参数名</th><th>描述</th><th>类型</th></tr>
		<tr><td>v</td><td>版本号,v=1</td><td>String</td></tr>
		<tr><td>an</td><td>appname</td><td>String</td></tr>
		<tr><td>p</td><td>手机类型，andriod传入1，ios传入2</td><td>int</td></tr>
		<tr><td>av</td><td>app版本</td><td>String</td></tr>
		<tr><td>pv</td><td>手机系统版本</td><td>String</td></tr>
		<tr><td>m</td><td>模块名</td><td>String</td></tr>
		<tr><td>l</td><td>错误等级，如ERROR, WARNING</td><td>String</td></tr>
		<tr><td>msg</td><td>crash的简单原因，后续统计根据msg进行分类，比如NullPointException</td><td>String</td></tr>
		<tr><td>db</td><td>手机品牌</td><td>String</td></tr>
		<tr><td>dm</td><td>手机型号</td><td>String</td></tr>
		<tr><td>id</td><td>dpid</td><td>String</td></tr>
		<tr><td>t</td><td>crash时间戳</td><td>long</td></tr>
		<tr><td>mi</td><td>混淆map id</td><td>String</td></tr>
		<tr><td>d</td><td>详细的错误日志</td><td>String</td></tr>
	</table>
	<h5>mobile-log-web中的crash上报接口：</h5><br/>
	<pre>http://stat.api.dianping.com/utm.js?v=\${appname}&mi=\${mapid}</pre>
	<table class="table table-bordered table-striped table-condensed  ">
		<tr><th>参数名</th><th>描述</th><th>类型</th></tr>
		<tr><td>v</td><td>appname,每个app有一个特定的名称</td><td>String</td></tr>
		<tr><td>mi</td><td>混淆map id</td><td>String</td></tr>
	</table>
	<pre class="text-danger">将错误的详细信息放在post的内容中。</pre>
	<br/>
	<h5>接收mobile-log-web发送的crash日志的接口：</h5>
	<pre>	http://{ip}/broker-service/api/crash</pre>
	<table class="table table-bordered table-striped table-condensed  ">
		<tr><th>参数名</th><th>描述</th><th>类型</th></tr>
		<tr><td>v</td><td>版本号，兼容之前的接口,v=2</td><td>String</td></tr>
		<tr><td>an</td><td>appname</td><td>String</td></tr>
		<tr><td>mi</td><td>混淆map id</td><td>String</td></tr>
		<tr><td>d</td><td>详细的错误日志</td><td>String</td></tr>
	</table>
	
<br/>

<h4 class="text-danger">APP Crash日志混淆map上传接口</h4>
	<pre>	http://{ip}/broker-service/api/crash?op=upload</pre>
	<p class="text-danger">参数可以post上来，需要对value进行encode。</p>
	<table class="table table-bordered table-striped table-condensed  ">
		<tr><th>参数名</th><th>描述</th><th>类型</th></tr>
		<tr><td>mi</td><td>混淆map ID</td><td>String</td></tr>
		<tr><td>f</td><td>混淆map内容</td><td>String</td></tr>
	</table>
<br/>

<br/>

<h4 class="text-danger">APP Crash日志统计获取接口</h4>
	<pre>	http://cat.dianpingoa.com/cat/r/app?op=appCrashLogJson</pre>
		<p>其他所有参数的设置可以在<a href="http://cat.dianpingoa.com/cat/r/app?op=appCrashLog">页面</a>点击查询后自动拼接生成，<span class="text-danger">将op参数设置为appCrashLogJson</span>就可以获取网页上的各个统计信息的JSON数据</p>
<br/>

<h4 class="text-danger">代码级日志上报接口</h4>
 <pre>
	beta: http://broker-service01.beta/broker-service/applog
	线上:  http://catdot.dianping.com/broker-service/applog</pre>
 	 <h5>HTTP Body : </h5>
	<p class="text-danger">key-value形式，json格式传上来</p>
	<table class="table table-bordered table-striped table-condensed  ">
	<tr><th>参数名</th><th>参数内容</th></tr>
	<tr><td>customParam</td>
		<td>
		key-value形式，json格式传上来<br/>
		appId: app在cat中的唯一ID，可在<a href="http://cat.dp/cat/s/app?op=appSources">APP来源</a>中查询或注册。<br/>
		appVersion: App版本号<br/>
		platVersion: 系统版本号<br/>
		deviceBrand: 设备品牌<br/>
		deviceModel: 设备硬件型号<br/>
		unionId: 用于查询个案的id值<br/>
		platform: 平台类型，分为android和ios<br/>
		</td>
	</tr>
	<tr><td>content</td>
		<td>
		key-value形式，json格式传上来，支持批量上传，每条日志的格式定义如下:<br/>
		time: 日志发生时间<br/>
		level: 日志类型，normal表示普通日志，error表示错误日志<br/>
		category: 用于聚合分类<br/>
		log: app日志的详细内容</td>
	</tr>
	</table>
	<pre>
	Http Body示例：
	{
	"content":
    		"[{"category":"testcategory","time":1463541358023,"level":"normal","log":"testlog"},
      		  {"category":"testcategory","time":1463541358023,"level":"normal","log":"testlog"}]",
	"customParam":
    		"{"platform":"ios",
     	 	"platVersion":"9.3.0",
      		"appId":"1",
      		"appVersion":"1.1.1",
      		"deviceBrand":"iphone",
      		"deviceModel":"8,0",
      		"unionId":"4765514784693486078"}"
	}
	</pre>
<br/>

<h4 class="text-danger">APP 长连访问批量上报接口</h4>
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
<br/>


<h4 class="text-danger">APP监控报表获取接口</h4>
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




	
	
	