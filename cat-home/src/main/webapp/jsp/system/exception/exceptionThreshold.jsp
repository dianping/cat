<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:body>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig.js']}" target="head-js" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
		 <res:useCss value="${res.css.local['jqx.base.css']}" target="head-css" />
	    <res:useJs value="${res.js.local['jqxcore.js']}" target="head-js" />
	    <res:useJs value="${res.js.local['jqxbuttons.js']}" target="head-js" />
	    <res:useJs value="${res.js.local['jqxscrollbar.js']}" target="head-js" />
	    <res:useJs value="${res.js.local['jqxlistbox.js']}" target="head-js" />
    <res:useJs value="${res.js.local['jqxcombobox.js']}" target="head-js" />

    
    
	<script type="text/javascript">
		$(document).ready(function() {
			$('#exceptionConfigList').addClass('active');
			$(".delete").bind("click", function() {
				return confirm("确定要删除此项目吗(不可恢复)？");
			});

	                  
			$(document).delegate('.update,.create', 'click', function(e){
				var anchor = this,
					el = $(anchor);
				
				if(e.ctrlKey || e.metaKey){
					return true;
				}else{
					e.preventDefault();
				}
				//var cell = document.getElementById('');
				$.ajax({
					type: "get",
					url: anchor.href,
					success : function(response, textStatus) {
						$('#myModal').html(response);
						$('#myModal').modal();
 						$("#domainId").select2();
					$("#exceptionId").select2();
					$("#hello").select2();
 						exceptionValidate();
 
	var source = [
	               "Affogato",
	               "Americano",
	               "Bicerin",
	               "Breve",
	               "Café Bombón",
	               "Café au lait",
	               "Caffé Corretto",
	               "Café Crema",
	               "Caffé Latte",
		        ];
	           // Create a jqxComboBox
	           $("#jqxcombobox").jqxComboBox({ source: source, selectedIndex: 0, width: '200px', height: '25px' });
	           // disable the sixth item.
	           $("#jqxcombobox").jqxComboBox('disableAt', 5); 
 
					}
				});
			});
			
	
			
			var action = '${payload.action.name}';
			if(action=='exceptionThresholdDelete'||action=='exceptionThresholdUpdateSubmit'){
				var state = '${model.opState}';
				if(state=='Success'){
					$('#state').html('操作成功');
				}else{
					$('#state').html('操作失败');
				}
				setInterval(function(){
					$('#state').html('&nbsp;');
				},3000);
			}
		});
	</script>




	<div>
		<div class="row-fluid">
        <div class="span2">
		<%@include file="../configTree.jsp"%>
		</div>
		<div class="span10">
			<!-- Modal -->
			<div id="myModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
			</div>
			<div>
			<table class="table table-striped table-bordered table-condensed table-hover" id="contents" width="100%">
			<thead>
				<tr class="odd">
					<th width="25%">域名</th>
					<th width="60%">异常名称</th>
					<th width="10%">Warning阈值</th>
					<th width="10%">Error阈值</th>
					<th width="5%">操作&nbsp;&nbsp;  <a class='create btn btn-primary btn-small' href="?op=exceptionThresholdUpdate">新增</a></th>
				</tr></thead><tbody>

				<c:forEach var="item" items="${model.exceptionLimits}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
					<td>${item.domain}</td>
					<td>${item.id}</td>
					<td>${item.warning}</td>
					<td>${item.error}</td>
					<td><a class='update btn  btn-small btn-primary'href="?op=exceptionThresholdUpdate&domain=${item.domain}&exception=${item.id}">编辑</a>
					<a class='delete btn  btn-small btn-danger' href="?op=exceptionThresholdDelete&domain=${item.domain}&exception=${item.id}">删除</a></td>
					</tr>
				</c:forEach></tbody>
				</tbody>
			</table>
		</div>
		</div></div></div>
</a:body>