<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-danger">用户端监控文档</h4>
<h5 class="text-info"> a).从用户端角度来看点评的业务接口状态，这是一个端到端的监控，能最早的发现用户端出现问题，比如根本访问不到点评，某城市延迟很大等。</h5>
<h5 class="text-info"> b).用户端的监控目前能监控Ajax接口，页面Page不能监控到。</h5>
<h5 class="text-info"> c).一般一个应用会监控1-2个重要接口，后端实时分析会按照城市、运营商维度做一些聚合分析。</h5>

<br/>
<h4>外部监控API文档</h4> 
<p>用途：提供外部监控的Http接口，用于监控用户端的错误信息。</p>
<p>1、为了保留以后的扩展性，移动端和Web端的暂定用不同的API接口。</p>
<p class="text-danger">2、公网IP，221.181.67.144 文档后面{ip}使用这个。</p>

<br/>
<h4 class="text-danger">Web单次接口</h4>
	<pre>	http://{ip}/web-broker-service/api/ajax</pre>
	
	<p>参数说明</p>
	<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
		<tr><th>query名</th><th>实际名称</th><th>描述</th><th>类型</th></tr>	
		<tr><td>v</td><td>version</td><td>API版本号</td><td>暂定为1</td></tr>
		<tr><td>ts</td><td>timestamp</td><td>发生时间</td><td>long型，1970到现在的毫秒数</td></tr>
		<tr><td>tu</td><td>targetUrl</td><td>调用的URL或API</td><td>String</td></tr>
		<tr><td>d</td><td>duration</td><td>访问耗时</td><td>long 毫秒</td></tr>
		<tr><td>c</td><td>code</td><td>返回结果码</td><td>整型</td></tr>
		<tr><td>s</td><td>requestByte</td><td>发送字节数</td><td>整型，以byte为单位</td></tr>
		<tr><td>r</td><td>responseByte</td><td>返回字节数</td><td>整型，以byte为单位</td></tr>
		<tr><td>n</td><td>network</td><td>网络类型</td><td>整型, 2G,3G,4G,WIFI(iOS只有3G和WIFI)，1=wifi, 2=2G, 3=3G, 4=4G, 0=Unknown</td></tr>
	</table>
<br/>

<br/>
<h4 class="text-danger">JS 错误上报接口</h4>
	<pre>	http://{ip}/web-broker-service/api/js</pre>
	
	<p>参数说明</p>
	<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
		<tr><th>query名</th><th>实际名称</th><th>描述</th><th>类型</th></tr>	
		<tr><td>v</td><td>version</td><td>API版本号</td><td>暂定为1</td></tr>
		<tr><td>t</td><td>timestamp</td><td>发生时间</td><td>long型，1970到现在的毫秒数</td></tr>
		<tr><td>msg</td><td>message</td><td>错误的类型,简要信息</td><td>String</td></tr>
		<tr><td>n</td><td>appName</td><td>错误的发生的应用模块</td><td>String</td></tr>
		<tr><td>l</td><td>level</td><td>错误等级</td><td>String,包括ERROR,WARN,INFO,DEV</td></tr>
		<tr><td>a</td><td>agent</td><td>浏览器信息</td><td>String</td></tr>
		<tr><td>id</td><td>dpid</td><td>用户ID，用于搜索错误日志</td><td>String</td></tr>
		<tr><td>data</td><td>data</td><td>详细出错信息</td><td>String，如果没有的话，传空串</td></tr>
	</table>
	<br/>

<h4 class="text-danger">Web原始日志上报接口</h4>
	<pre>	http://{ip}/web-broker-service/api/log</pre>
	
	批量接口POST内容，前面加上v=1&c=，不同请求之间用回车<span class="text-danger">ENTER</span>分隔，字段之间用<span class="text-danger">TAB</span>分隔。

	<pre>
	timstamp<span class="text-danger">TAB</span>level<span class="text-danger">TAB</span>requestId<span class="text-danger">TAB</span>appName<span class="text-danger">TAB</span>url<span class="text-danger">TAB</span>message<span class="text-danger">ENTER</span>
	
	sample如下:
	
	v=1&c=
	1400037748182<span class="text-danger">TAB</span>ERROR<span class="text-danger">TAB</span>11233333<span class="text-danger">TAB</span>shopInfo<span class="text-danger">TAB</span>url1<span class="text-danger">TAB</span>test1<span class="text-danger">ENTER</span>
	1400037748182<span class="text-danger">TAB</span>INFO<span class="text-danger">TAB</span>22339283<span class="text-danger">TAB</span>shopInfo<span class="text-danger">TAB</span>url2<span class="text-danger">TAB</span>test2<span class="text-danger">ENTER</span>
	1400037748182<span class="text-danger">TAB</span>WARN<span class="text-danger">TAB</span>13456664<span class="text-danger">TAB</span>shopInfo<span class="text-danger">TAB</span>url3<span class="text-danger">TAB</span>test3<span class="text-danger">ENTER</span>
	</pre>
	<p class="text-danger">v=1&c=不需要做urlencode,后面的批量的content部分需要urlencode。</p>
	<p>参数说明</p>
	<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
		<tr><th>实际名称</th><th>描述</th><th>类型</th></tr>	
		<tr><td>v</td><td>API版本号</td><td>暂定为1</td></tr>
		<tr><td>c</td><td>具体内容</td><td>content内容</td></tr>
	</table>
	<p>content内容说明</p>
	<pre>
	timestamp<span class="text-danger">TAB</span>level<span class="text-danger">TAB</span>requestId<span class="text-danger">TAB</span>appName<span class="text-danger">TAB</span>Url<span class="text-danger">TAB</span>message<span class="text-danger">ENTER</span>
	</pre>
	<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
		<tr><th>实际名称</th><th>描述</th><th>类型</th></tr>	
		<tr><td>timestamp</td><td>发生时间</td><td>long型，1970到现在的毫秒数</td></tr>
		<tr><td>level</td><td>log等级</td><td>DEV,INFO,WARN,ERROR</td></tr>
		<tr><td>requestId</td><td>用户id</td><td>String</td></tr>
		<tr><td>appName</td><td>应用名</td><td>String</td></tr>
		<tr><td>Url</td><td>请求访问的URL</td><td>String</td></tr>
		<tr><td>message</td><td>日志信息</td><td>String</td></tr>
	</table>
<br/>
	
<h4 class="text-danger">Web测速上报接口</h4>
<pre>	http://{ip}/web-broker-service/api/speed</pre>
	
<p>参数说明</p>
<table style="width:90%" class="table table-bordered table-striped table-condensed  ">
		<tr><th style="width:10%">query名</th><th style="width:15%">实际名称</th><th style="width:15%">描述</th><th style="width:60%">类型</th></tr>	
		<tr><td>v</td><td>version</td><td>API版本号</td><td>暂定为2</td></tr>
		<tr><td>t</td><td>timestamp</td><td>发生时间</td><td>long型，1970到现在的毫秒数</td></tr>
		<tr><td>n</td><td>network</td><td>网络类型</td><td>整型, 2G,3G,4G,WIFI(iOS只有3G和WIFI)，1=wifi, 2=2G, 3=3G, 4=4G, 0=Unknown</td></tr>
		<tr><td>p</td><td>platform</td><td>平台类型</td><td>整型</td></tr>
		<tr><td>source</td><td>source</td><td>来源</td><td>整型</td></tr>
		<tr><td>w</td><td>page</td><td>web页面id，可于配置页面查询</td><td>整型</td></tr>
		<tr><td>speedparams</td><td>测速点</td><td>详细测速信息</td><td>以测速点编号-时间为一个单元，每个测速点之间以\t分隔， step1-responseTime1<span class="text-danger">TAB</span>step2-responseTime2...,例如1-1\t2-10\t3-100表明编号为1的测速点加载时间1毫秒，编号为2的测速点，加载时间10毫秒...</td></tr>
</table>
<br/>

<h4 class="text-danger">Web测速 日报表数据获取接口</h4>
  1.测速点获取
	<pre> http://cat.dp/cat/r/browser?op=speedConfigFetch&type={type}</pre>
	type可为xml或json,分别返回相应格式的数据。<br/>
	json数据示例：
	<pre>
{
    "speeds": {
        "1": {
            "id": 1,
            "page": "testpage",
            "steps": {
                "1": {
                    "id": 1,
                    "title": "unloadEventStart"
                },
                "2": {
                    "id": 2,
                    "title": "unloadEventEnd"
                }
            }
        },
        "2": {
            "id": 2,
            "page": "homepage",
            "steps": {
                "1": {
                    "id": 1,
                    "title": "unloadEventStart"
                },
                "2": {
                    "id": 2,
                    "title": "unloadEventEnd"
                }   
            }
        }
    }
}
</pre>
  2.数据获取
 	<pre> http://cat.dp/cat/r/browser?op=speedJson&query1={query}</pre>
 
query条件如下：
<pre>
{date};{page};{stepId};{network};{platform};{city};{operator};{source}

date格式为'YYYY-MM-DD'
page由pageId和pageName组成，中间用竖线"|"分隔
stepId为测速点ID
network、platform、city、operator、source均为整型，具体数值含义见 http://cat.dp/cat/s/web?op=webConstants,不传默认为查询全部数据
</pre>
示例：
<pre>
http://cat.dp/cat/r/browser?op=speedJson&query1=2015-11-25;1|testpage;1;;;;;
该示例表明查询2015年11月25日,pageId为1，pageName为testpage，测速点编号为1的测速点数据。

返回数据示例如下：
{
    "webSpeedSummarys": {
        "当前值": {
            "period": "2015-11-25 00:00:00",
            "minuteOrder": 0,
            "accessNumberSum": 781,
            "responseTimeAvg": 2.381562099871959
        }
    },
    "webSpeedDetails": {
        "当前值": [
            {
                "period": "2015-11-25 00:00:00",
                "minuteOrder": 0,
                "accessNumberSum": 30,
                "responseTimeAvg": 1.6
            },
            {
                "period": "2015-11-25 00:00:00",
                "minuteOrder": 5,
                "accessNumberSum": 39,
                "responseTimeAvg": 3.3076923076923075
            }
          ...
        ]
    }
}
</pre>
webSpeedSummarys中为当天数据的聚合值，webSpeedDetails中存放一天中每五分钟的速度均值。
<br/>



	
	
	