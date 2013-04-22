<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.home.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.home.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.home.Model" scope="request"/>

<a:body>
<c:choose>
<c:when test="${not empty model.content}">
${model.content}
</c:when>
<c:otherwise>
Welcome to <b>Central Application Tracking (CAT)</b>.
<br>
<br>
<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js"/>
<table class='table table-striped table-bordered table-condensed'>
	<tr><td>CAT系统文档</td><td>CAT系统链接</td></tr>
	<tr><td><a href="http://wiki.dianpingoa.com/bin/view/SOA%E6%9E%B6%E6%9E%84/CAT%E7%94%A8%E6%88%B7%E6%89%8B%E5%86%8C" target="_blank">CAT用户手册</a></td>
		<td><a href="http://cat.qa.dianpingoa.com/cat/r">CAT线下环境链接</a></td>	</tr>
	<tr><td><a href="http://wiki.dianpingoa.com/bin/view/SOA%E6%9E%B6%E6%9E%84/CAT%E5%BC%80%E5%8F%91%E8%80%85%E6%96%87%E6%A1%A3" target="_blank">CAT开发者文档</a></td>
	<td><a href="http://10.1.8.64:8080/cat/r">CAT预发环境链接</a></td>	</tr>
	<tr><td><a href="http://wiki.dianpingoa.com/bin/view/SOA%E6%9E%B6%E6%9E%84/CAT%E9%9B%86%E6%88%90%E5%B8%AE%E5%8A%A9%E6%96%87%E6%A1%A3" target="_blank">CAT集成帮助文档</a></td>	
		<td><a href="http://cat.dianpingoa.com/cat/r">CAT线上环境链接</a></td></tr>
</table>
<br>
<table class='table table-striped table-bordered table-condensed'>
	<tr><td width="3%">序号</td><td width="87%">最新发布功能描述</td><td width="10%">发布时间</td></tr>	
	<tr><td>8</td><td style="color:red">Problem小时报表支持一个小时内的错误趋势图</td><td>2013-03-13</td></tr>
	<tr><td>7</td><td style="color:red">Query报表支持按照天或者小时查询Transaction,Event,Problem数据</td><td>2013-03-11</td></tr>
	<tr><td>6</td><td style="color:red">Cross报表支持根据方法名称查询是哪些客户端调用此方法</td><td>2013-03-11</td></tr>
	<tr><td>5</td><td style="color:red">Top报表,根据分钟级别实时展现线上异常最多、访问最慢(URL\Service\SQL\Call\Cache)的应用</td><td>2013-03-11</td></tr>
	<tr><td>4</td><td style="color:red">项目信息修改，请项目负责人到Project标签下，修改项目所在分组的基本信息（仅修改线上环境）</<td><td>2013-01-21</td></tr>
	<tr><td>3</td><td>Transaction\Event月报表支持每天的趋势图，以天为单位</td><td>2013-01-21</td></tr>
	<tr><td>2</td><td>Transaction\Event报表日报表、周报表支持趋势图对比,时间精度为5分钟</td><td>2013-01-01</td></tr>
	<tr><td>1</td><td>默认告警，邮件订阅（修改线上环境即可)，请项目负责人到Alarm标签下，订阅相关异常告警、服务调用失败告警、日常邮件，Hawk会逐步下线中。</td><td>2012-09-01</td></tr>
</table>
<br>
<table class='table table-striped table-bordered table-condensed'>
	<tr><td width="3%">序号</td><td>常见问题</td><td>问题解答</td></tr>
	<tr  style="color:red"><td>7</td><td>Job打包出错</<td><td style="white-space:normal;">升级maven-assembly-plugin至2.2-beta-5版本</td></tr>
	<tr><td>6</td><td>95Line</<td><td style="white-space:normal;">由于计算95line需要数据较多，为了减少内存开销，系统做了一些优化，在单个小时内某一台机器，95Line误差是1ms，但是合并成1一天，1周，1个月误差较大（数据仅供参考）</td></tr>
	<tr><td>5</td><td>默认告警，邮件订阅（仅修改线上环境）</<td><td>请项目负责人到Alarm标签下，订阅相关异常告警、服务调用失败告警、日常邮件，Hawk会逐步下线中。</td></tr>
	<tr><td>4</td><td>Problem、Hearbeat报表区间</<td><td>Problem、Heartbeat历史报表，图表X轴以1分钟为刻度，Y轴表示每1分钟的值</td></tr>
	<tr><td>3</td><td>Transaction、Event报表区间</<td><td>Transaction、Event历史报表，图表X轴以5分钟为刻度，Y轴表示每5分钟的值</td></tr>
	<tr><td>2</td><td>集成了CAT，测试环境看不到信息</td><td>请Check相关步骤<a href="http://wiki.dianpingoa.com/bin/view/SOA%E6%9E%B6%E6%9E%84/CAT%E9%9B%86%E6%88%90%E5%B8%AE%E5%8A%A9%E6%96%87%E6%A1%A3" target="_blank">CAT集成帮助文档</a></td></tr>
	<tr><td>1</td><td>TestCase跑不起来</td><td>升级至CAT的0.4.0的版本，Pigeon版本至1.6.1</td></tr>
</table>
<br>
<table class='table table-striped table-bordered table-condensed'>
	<tr><td width="3%">版本</td><td width="87%">说明</td><td width="10%">发布时间</td></tr>
	<tr><td>0.6.0</td><td>1、增加业务监控埋点API。2、修复时间戳调整bug。3、修复classpath获取bug。4、修复CatFilter支持Forward请求</td><td>2013-03-26</td></tr>
	<tr><td>0.4.1</td><td>1、默认禁止心跳线程获取线程锁信息，以降低对业务线程的影响。</td><td>2012-09-06</td></tr>
	<tr><td>0.4.0</td><td>1、支持开关动态关闭。2、后端存储重构，支持分布式Logview的查看(关联pigeon的call)。</td><td>2012-08-20</td></tr>
	<tr><td>0.3.4</td><td>1、规范了CAT客户端的日志。2、规范了后台模块的加载顺序。3、统一服务端配置存取。4、新增心跳报表的Http线程 </td><td>2012-07-25</td></tr>
	<tr><td>0.3.3</td><td>1、修改CAT线程为后台Dameon线程。2、减少CAT的日志输出。3、修复了极端情况客户端丢失部分消息。4、支持CAT的延迟加载。5、修复了0.3.2一个getLog的bug</td><td>2012-07-17</td></tr>
	<tr><td>0.3.2</td><td>1、修复了配置单个服务器时候，服务器重启，客户端断开链接bug。2、修复了CAT不正常加载时候，内存溢出的问题。（此版本有问题，请更新至0.3.3）</td><td>2012-07-01</td></tr>
	<tr><td>0.3.1</td><td>1、修复CAT在业务testcase的使用，支持业务运行Testcase在Console上看到运行情况。</td><td>2012-06-25</td></tr>
	<tr><td>0.3.0</td><td>1、修复CAT在Transaction Name的Nullpoint异常。</td><td>2012-06-15</td></tr>
	<tr><td>0.2.5</td><td>1、心跳消息监控新增oldgc和newgc  2、更新了ThreadLocal的线程模型（修复了一些无头消息和部分错乱消息）</td><td>2012-05-01</td></tr>
</table>
<br>
<br>
<br>
<br>
<br>
<br>
<a href="?op=checkpoint&domain=${model.domain}&date=${model.date}" style="color:#FFF">Do checkpoint here</a>
</c:otherwise>
</c:choose>

</a:body>