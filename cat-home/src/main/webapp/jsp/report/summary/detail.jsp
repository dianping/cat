<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="span10">
	<!-- Modal -->
	<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	  <div class="modal-dialog">
	    <div class="modal-content">
	      <div class="modal-body">
	      </div>
	    </div>
	  </div>
	</div>
	<br>
	<form id="form" method="post" action="?op=summary">
		警告时间
		<input type="text" name="summarytime" id="summarytime" value="<fmt:formatDate value="${payload.summarytime}" pattern="yyyy-MM-dd HH:mm"/>" style="height:auto" class="input-medium" placeholder="格式：2014-07-01 00:00">
		应用名
		<input type="text" name="summarydomain" id="summarydomain" value="${payload.summarydomain}" style="height:auto" class="input-small">
		发送邮箱
		<input type="text" name="summaryemails" id="summaryemails" value="${payload.summaryemails}" style="height:auto;width:200px" class="input-small" placeholder="用半角逗号分割，可为空"> 
		<input class="btn btn-primary  btn-small"  value="查询" type="submit">
	</form>
	${model.summaryContent}
</div>