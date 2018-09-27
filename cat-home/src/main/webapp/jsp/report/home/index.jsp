<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-success">CAT总体介绍</h4>
<h5>CAT(Central Application Tracking)是基于Java开发的实时应用监控平台，为美团点评提供了全面的监控服务和决策支持。
</h5>
<h5>CAT作为美团点评基础监控组件，它已经在中间件框架（MVC框架，RPC框架，数据库框架，缓存框架等）中得到广泛应用，为美团点评各业务线提供系统的性能指标、健康状况、基础告警等。</h5>
</br>
<h4 class="text-success">CAT目前现状</h4>
	<ul>
		<li>15台CAT物理监控集群</li>
		<li>单台机器15w qps</li>
		<li>2000+ 业务应用（包括部分.net以及Job）</li>
		<li>7000+ 应用服务器</li>
		<li>50TB 消息，~450亿消息（每天）</li>
	</ul>
	<br/>
<h4 class="text-success">CAT监控大盘</h4>
<div>
	<a id="navmetricDashboard" class="btn btn-sm btn-primary" target="_blank" href="/cat/r/top?op=view&domain=${model.domain}">系统报错大盘</a>
	<%--<a id="navdashboard" class="btn btn-sm btn-primary" target="_blank" href="/cat/r/dependency?op=dashboard&domain=${model.domain}">应用监控大盘</a>--%>
	<%--<a id="navbussiness" class="btn btn-sm btn-primary" target="_blank" href="/cat/r/metric?op=dashboard&domain=${model.domain}">业务监控大盘</a>--%>
	<%--<a id="navbussiness" class="btn btn-sm btn-primary" target="_blank" href="/cat/r/network?op=dashboard&domain=${model.domain}">网络监控大盘</a>--%>
	<%--<a id="navbussiness" class="btn btn-sm btn-primary" target="_blank" href="/cat/r/storage?op=dashboard&domain=${model.domain}">数据库监控大盘</a>--%>
</div>
</br>
<%--<h4 class="text-success">CAT其他环境</h4>--%>
<%--<div>--%>
	<%--<a class="btn btn-sm btn-primary" href="http://cat.qa.dianpingoa.com/cat/r/">测试环境</a>--%>
	<%--<a class="btn btn-sm btn-primary" href="http://ppe.cat.dp/cat/r/">PPE环境</a>--%>
	<%--<a class="btn btn-sm btn-primary" href="http://cat.dianpingoa.com/cat/r/">生产环境</a>--%>
<%--</div>--%>
<%--</br>--%>
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
