<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<form id="form" method="post" action="?op=summary">
	<div style="float:left;">
	&nbsp;&nbsp;时间
	<input type="text" name="summarytime" id="summarytime" value="<fmt:formatDate value="${payload.summarytime}" pattern="yyyy-MM-dd HH:mm"/>" style="width:130px;"/> </div>
	&nbsp;应用名
	<input type="text" name="summarydomain" id="summarydomain" value="${payload.summarydomain}" style="height:auto" class="input-small">
	发送邮箱
	<input type="text" name="summaryemails" id="summaryemails" value="${payload.summaryemails}" style="height:auto;width:200px" class="input-small" placeholder="用半角逗号分割，可为空"> 
	<input class="btn btn-primary  btn-sm"  value="查询" type="submit">
</form>
${model.summaryContent}
