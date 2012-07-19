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
<br>
<table>
	<tr><td><a href="http://192.168.7.43:9080/bin/view/soa-110-cat/userefrrence" target="_blank">CAT用户手册</a></td>	</tr>
	<tr><td><a href="http://192.168.7.43:9080/bin/view/soa-110-cat/cat-developer-cat" target="_blank">CAT开发者文档</a></td>	</tr>
	<tr><td><a href="http://192.168.7.43:9080/bin/view/soa-110-cat/CATIntegration" target="_blank">CAT集成帮助文档</a></td>	</tr>
</table>
<br>
<br>
<br>
<table class='version'>
	<tr class="odd"><td>版本</td><td>说明</td></tr>
	<tr class="even"><td>0.3.3</td><td>1、修改CAT线程为后台Dameon线程。2、减少CAT的日志输出。3、修复了极端情况客户端丢失部分消息。4、支持CAT的延迟加载。</td></tr>
	<tr class="even"><td>0.3.2</td><td>1、修复了配置单个服务器时候，服务器重启，客户端断开链接bug。2、修复了CAT不正常加载时候，内存溢出的问题。</td></tr>
	<tr class="even"><td>0.3.1</td><td>1、修复CAT在业务testcase的使用，支持业务运行Testcase在Console上看到运行情况。</td></tr>
	<tr class="odd"><td>0.3.0</td><td>1、修复CAT在Transaction Name的Nullpoint异常。</td></tr>
	<tr class="even"><td>0.2.5</td><td>1、心跳消息监控新增oldgc和newgc
						  2、更新了ThreadLocal的线程模型（修复了一些无头消息和部分错乱消息）
                       	</td></tr>
</table>
<br>
<br>
<br>
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