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
	<tr><td><a href="http://192.168.7.43:9080/bin/view/soa-110-cat/userefrrence">CAT用户手册</a></td>	</tr>
	<tr><td><a href="http://192.168.7.43:9080/bin/view/soa-110-cat/cat-developer-cat">CAT开发者文档</a></td>	</tr>
	<tr><td><a href="http://192.168.7.43:9080/bin/view/soa-110-cat/CATIntegration">CAT集成帮助文档</a></td>	</tr>
</table>
<br>
<br>
<br>
<table class='version'>
	<tr class="odd"><td>版本</td><td>说明</td></tr>
	<tr class="even"><td>0.3.1</td><td>1、优化了CAT的在业务testcase的优化，自动延迟初始化</td></tr>
	<tr class="odd"><td>0.3.0</td><td>1、优化了CAT在团购上线过程Nullpoint异常，由于SQL监控的名称为Null</td></tr>
	<tr class="even"><td>0.2.5</td><td>1、监控新增 oldgc和newgc
						  2、更新了ThreadLocal的线程模型（修复了一些无头消息和部分错乱消息）
                          3、将CAT的线程开关提升到500</td></tr>
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