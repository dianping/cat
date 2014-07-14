<%@ page contentType="text/html; charset=utf-8" %>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.summary.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.summary.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.summary.Model" scope="request"/>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css">

		<form id="form" method="post" action="summary?op=view">
			警告时间
			<input type="text" name="time" id="time" value="<fmt:formatDate value="${payload.time}" pattern="yyyy-MM-dd HH:mm:ss"/>" style="height:auto" class="input-medium" placeholder="格式如：2014-07-01 00:00:00">
			应用名
			<input type="text" name="domain" id="domain" value="${payload.domain}" style="height:auto" class="input-small">
			发送邮箱
			<input type="text" name="emails" id="emails" value="${payload.emails}" style="height:auto" class="input-small" placeholder="用半角逗号分割，可为空"> 
			<input class="btn btn-primary  btn-small"  value="查询" type="submit">
		</form>
		${model.summaryContent}
