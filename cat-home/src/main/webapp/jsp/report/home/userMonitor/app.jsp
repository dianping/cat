<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-error">1、APP端到端使用说明&nbsp;&nbsp;&nbsp;&nbsp; <a href="/cat/r/app?domain=cat&ip=All&reportType=&op=view">访问链接</a></h4>
<p>监控点评APP的接口调用情况，这个是从用户手机APP采集的数据，从用户角度看点评接口的访问速度。</p>
<p>监控的分析的维度有返回码、网络类型、APP版本、平台、地区、运营商等。</p>
</br>

<h5 class="text-error">Sample1：下图显示了shop.bin在不同平台的访问量的对比情况。</h5>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor01.png"/>

<h5 class="text-error">Sample2：下图显示可以按照不同维度展开的OLAP功能，下图按照运营商维度展开，看不同接口的访问情况。</h5>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor03.png"/>

</br></br>
<h4 class="text-error">2、APP端到端配置&nbsp;&nbsp;&nbsp;&nbsp; <a href="/cat/s/config?op=appList">访问链接</a> </h4>
<p>用户可以在该界面对Command命令字进行修改操作。</p>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor05.png"/>
</br></br>
<h4 class="text-error">3、APP命令字添加/删除API&nbsp;&nbsp;&nbsp;&nbsp; </h4>
<p>Cat支持其它系统通过调用HTTP API来添加、删除APP命令字</p>
<pre>
	http请求方式: GET或者POST
	http://主机域名:端口/cat/s/config?
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
</br></br>
<h4 class="text-error">4、APP端到端告警&nbsp;&nbsp;&nbsp;&nbsp;<a href="/cat/s/config?op=appRule">访问链接</a></h4>
<h5 class="text-success">A) 配置一览表</h5>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor06.png"/>
<h5 class="text-success">B) 配置告警规则</h5>
<p>（1）告警名自定义，方便区分告警项。可对<span class="text-error">请求数、访问成功率、响应时间</span>进行监控。</p>
<p>（2）多个监控规则构成了告警的主体，分别对不同时间段进行配置，以方便准确地进行告警。</p>
<p>（3）监控规则诠释着某个时间段内如何进行告警，由任意多个监控条件组成。任何一条监控条件触发都会引起监控规则触发，从而告警。</p>
<p>（4）监控条件诠释着什么条件会触发监控规则，由任意多个监控子条件组成。当所有子条件同时被触发时，才会触发该监控规则。</p>

<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor07.png"/>



