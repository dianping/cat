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
<table>
	<tr><td><a href="http://wiki.dianpingoa.com/bin/view/SOA%E6%9E%B6%E6%9E%84/CAT%E7%94%A8%E6%88%B7%E6%89%8B%E5%86%8C" target="_blank">CAT用户手册</a></td>	</tr>
	<tr><td><a href="http://wiki.dianpingoa.com/bin/view/SOA%E6%9E%B6%E6%9E%84/CAT%E5%BC%80%E5%8F%91%E8%80%85%E6%96%87%E6%A1%A3" target="_blank">CAT开发者文档</a></td>	</tr>
	<tr><td><a href="http://wiki.dianpingoa.com/bin/view/SOA%E6%9E%B6%E6%9E%84/CAT%E9%9B%86%E6%88%90%E5%B8%AE%E5%8A%A9%E6%96%87%E6%A1%A3" target="_blank">CAT集成帮助文档</a></td>	</tr>
</table>
<br>
<br>
<table class='version'>
	<tr class="odd"><td>版本</td><td>说明</td><td>发布时间</td></tr>
	<tr class="even"><td>0.4.0</td><td>1、支持开关动态关闭。2、支持分布式Logview的查看(关联pigeon的call)。</td><td>2012-08-20</td></tr>
	<tr class="odd"><td>0.3.4</td><td>1、规范了CAT客户端的日志。2、规范了后台模块的加载顺序。3、统一服务端配置存取。4、新增心跳报表的Http线程 </td><td>2012-07-25</td></tr>
	<tr class="even"><td>0.3.3</td><td>1、修改CAT线程为后台Dameon线程。2、减少CAT的日志输出。3、修复了极端情况客户端丢失部分消息。4、支持CAT的延迟加载。5、修复了0.3.2一个getLog的bug</td><td>2012-07-17</td></tr>
	<tr class="odd"><td>0.3.2</td><td>1、修复了配置单个服务器时候，服务器重启，客户端断开链接bug。2、修复了CAT不正常加载时候，内存溢出的问题。（此版本有问题，请更新至0.3.3）</td><td>2012-07-01</td></tr>
	<tr class="even"><td>0.3.1</td><td>1、修复CAT在业务testcase的使用，支持业务运行Testcase在Console上看到运行情况。</td><td>2012-06-25</td></tr>
	<tr class="odd"><td>0.3.0</td><td>1、修复CAT在Transaction Name的Nullpoint异常。</td><td>2012-06-15</td></tr>
	<tr class="even"><td>0.2.5</td><td>1、心跳消息监控新增oldgc和newgc  2、更新了ThreadLocal的线程模型（修复了一些无头消息和部分错乱消息）</td><td>2012-05-01</td></tr>
</table>
<br>
<br>
<table>
	<tr class="even"><td>序号</td><td>常见问题</td><td>解答</td></tr>
	<tr class="odd"><td>1</td><td>TestCase跑不起来</td><td>升级至CAT的0.4.0的版本，Pigeon版本至1.6.1</td></tr>
	<tr class="even"><td>2</td><td>集成了CAT，但是在测试环境看不到信息</td><td>请check相关步骤<a href="http://wiki.dianpingoa.com/bin/view/SOA%E6%9E%B6%E6%9E%84/CAT%E9%9B%86%E6%88%90%E5%B8%AE%E5%8A%A9%E6%96%87%E6%A1%A3" target="_blank">CAT集成帮助文档</a></td></tr>
	<tr class="odd"><td>3</td><td>Transaction、Event报表区间</<td><td>Transaction、Event历史报表，图表X轴以5分钟为刻度，Y轴表示每5分钟的值</td></tr>
	<tr class="even"><td>4</td><td>Problem、Hearbeat报表区间</<td><td>Problem、Heartbeat历史报表，图表X轴以1分钟为刻度，Y轴表示每1分钟的值</td></tr>
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