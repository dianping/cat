<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.app.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.app.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.app.Model" scope="request"/>

<a:mobile>
	<script type="text/javascript">
		$('#batchInsert').bind("click",function(e){
			if (confirm("确认要进行批量添加吗？") == true){
				var items = document.getElementsByClassName('deleteItem');
				var content = "";
				var length = items.length;
				
				for(var i=0;i<length;i++){
					var item = items[i];
					if(item.checked == true){
						content = content + item.value + ",";
					}
				}
				window.location.href = "?op=appRuleBatchUpdate&type=batch&content="+content;
			}		
		});
		
		$(document).ready(function() {
			$('#userMonitor_config').addClass('active open');
			$('#appCommandBatch').addClass('active');
			
			var state = '${model.opState}';
			if(state=='Success'){
				$('#state').html('操作成功');
			}else{
				$('#state').html('操作失败');
			}
			setTimeout(function(){
				$('#state').html('&nbsp;');
			},3000);
 		});
	</script>
	<h4 class="text-center text-danger">合法的命令字&nbsp;&nbsp;${w:size(model.validatePaths)}</h4>
	<table class="table table-striped table-condensed table-bordered  table-hover" id="contents" width="100%">
			<tr><td></td><td><button class="btn btn-xs btn-danger" id="batchInsert">批量添加</button></td></tr>
		<c:forEach var="item" items="${model.validatePaths}">
			<tr><td width="10%"><input type="checkbox" class="deleteItem" value="${item}" checked></td><td>${item}</td><tr>
		</c:forEach>
	</table>
	
	<h4 class="text-center text-danger">非法命令字&nbsp;&nbsp;${w:size(model.invalidatePaths)}</h4>
	
	<table class="table table-striped table-condensed table-bordered  table-hover" id="contents" width="100%">
		<c:forEach var="item" items="${model.invalidatePaths}">
			<tr><td width="10%"><input type="checkbox" class="deleteItem" value="${item}"></td><td>${item}</td><tr>
		</c:forEach>
	</table>
</a:mobile>