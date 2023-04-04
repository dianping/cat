<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-success"><b>CAT 实时监控平台</b></h4>
<h5>CAT（Central Application Tracking）是美团点评开源的实时应用监控平台，提供了 Tracsaction、Event、Problem、Business
	等丰富的指标项。
</h5>
<br/>
<h4 class="text-success">功能特性</h4>
<ul>
	<li>链路跟踪：通过 TraceId 搜索消息树，定位问题更高效。</li>
	<li>告警扩展：新增钉钉、企业微信、飞书机器人推送支持，邮件发送支持直连模式。</li>
	<li>高度集成：客户端引入组件后，只需要开启配置，就完成了 HTTP、Dubbo、Redis、SQL、Log4j2、MQ 埋点。</li>
	<li>界面调整：术语汉化、开放彩蛋、LOGO和消息树美化（陆续优化中）</li>
</ul>
<br/>

<h4 class="text-info">链路跟踪</h4>
<img class="img-polaroid" src="${model.webapp}/images/sample/tracing.png" style="width: 100%"/>
<br/>
<br/>
<h4 class="text-info">告警推送</h4>
<img class="img-polaroid" src="${model.webapp}/images/sample/dingtalk.png"/>
<img class="img-polaroid" src="${model.webapp}/images/sample/mail.png"/>

<%--</br>
<h4 class="text-success">CAT目前现状</h4>
	<ul>
		<li>15台CAT物理监控集群</li>
		<li>单台机器15w qps</li>
		<li>2000+ 业务应用（包括部分.net以及Job）</li>
		<li>7000+ 应用服务器</li>
		<li>50TB 消息，~450亿消息（每天）</li>
	</ul>
	<br/>--%>
<%--<h4 class="text-success">CAT监控大盘</h4>
<div>
	<a id="navmetricDashboard" class="btn btn-sm btn-primary" target="_blank" href="/cat/r/top?op=view&domain=${model.domain}">系统报错大盘</a>
	<a id="navdashboard" class="btn btn-sm btn-primary" target="_blank" href="/cat/r/dependency?op=dashboard&domain=${model.domain}">应用监控大盘</a>
	<a id="navbussiness" class="btn btn-sm btn-primary" target="_blank" href="/cat/r/metric?op=dashboard&domain=${model.domain}">业务监控大盘</a>
	<a id="navnetwork" class="btn btn-sm btn-primary" target="_blank"
	   href="/cat/r/network?op=dashboard&domain=${model.domain}">网络监控大盘</a>
	<a id="navdatabase" class="btn btn-sm btn-primary" target="_blank"
	   href="/cat/r/storage?op=dashboard&domain=${model.domain}">数据库监控大盘</a>
</div>
</br>--%>
<%--<h4 class="text-success">CAT其他环境</h4>--%>
<%--<div>--%>
	<%--<a class="btn btn-sm btn-primary" href="http://localhost/cat/r/">测试环境</a>--%>
	<%--<a class="btn btn-sm btn-primary" href="http://ppe.cat.dp/cat/r/">PPE环境</a>--%>
	<%--<a class="btn btn-sm btn-primary" href="http://cat.dianpingoa.com/cat/r/">生产环境</a>--%>
<%--</div>--%>
<%--</br>--%>
<%--
<h4 class="text-danger">更多接入公司，欢迎在<a href="https://github.com/dianping/cat/issues/753" target="_blank">登记！</a></h4>
<table>
	<tr>
		<td><a target="_blank" href="http://www.dianping.com/"><img  class="img-polaroid"  src="${model.webapp}/images/logo/dianping.png"/></a></td>
		<td><a target="_blank" href="http://www.ctrip.com/"><img  class="img-polaroid"  src="${model.webapp}/images/logo/ctrip.png"/></a></td>
		<td><a target="_blank" href="https://www.lufax.com/"><img  class="img-polaroid"  src="${model.webapp}/images/logo/lufax.png"/></a></td>
		<td><a target="_blank" href="http://www.ly.com/"><img  class="img-polaroid"  src="${model.webapp}/images/logo/ly.png"/></a></td>
	</tr>
	<tr>
		<td><a target="_blank" href="http://www.liepin.com/"><img  class="img-polaroid"  src="${model.webapp}/images/logo/liepin.png"/></a></td>
		<td><a target="_blank" href="http://www.qipeipu.com/"><img  class="img-polaroid"  src="${model.webapp}/images/logo/qipeipu.jpg"/></a></td>
		<td><a target="_blank" href="http://www.shangpin.com/"><img  class="img-polaroid"  src="${model.webapp}/images/logo/shangping.jpg"/></a></td>
		<td><a target="_blank" href="http://www.travelzen.com/flight/"><img  class="img-polaroid"  src="${model.webapp}/images/logo/zhenlv.png"/></a></td>
		<td></td>
	</tr>
	<tr>
		<td><a target="_blank" href="http://www.oppo.com/"><img  class="img-polaroid"  src="${model.webapp}/images/logo/oppo.png"/></a></td>
	</tr>
</table>
--%>
