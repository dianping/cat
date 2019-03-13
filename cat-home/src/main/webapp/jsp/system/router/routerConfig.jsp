<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:config>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['editor.js']}" target="head-js" />
	<script src='${model.webapp}/assets/js/editor/ace.js'></script>

			<form name="routerConfigUpdate" id="form" method="post"
				action="${model.pageUri}?op=routerConfigUpdate">
				<table class="table table-striped table-condensed  table-hover">
					<tr><td>
					<input id="content" name="content" value="" type="hidden"/>
					<div id="editor" class="editor">${model.content}</div>
					</td></tr>
					<tr>
						<td  style="text-align:center"><input class='btn btn-primary' id="addOrUpdateEdgeSubmit"
							type="submit" name="submit" value="提交" /> &nbsp; &nbsp;&nbsp;&nbsp;
							<a href="/cat/s/router?op=build" class='btn btn-primary' id="routerRebuild" target="_blank">重算路由</a></td>
					</tr>
				</table>
			</form>
			<h4 class="text-center text-danger" id="state">&nbsp;</h4>
</a:config>
<script type="text/javascript">
		$("#routerRebuild").on('click', function(e) {
			e.preventDefault();
			var anchor = this;
			var dialog = $( "#rebuild-router-message" ).removeClass('hide').dialog({
				modal: true,
				title: "<div class='widget-header widget-header-small'><h4 class='smaller'><i class='ace-icon fa fa-check'></i>CAT提示</h4></div>",
				title_html: true,
				buttons: [ 
					{
						text: "Cancel",
						"class" : "btn btn-xs",
						click: function() {
							$( this ).dialog( "close" ); 
						} 
					},
					{
						text: "OK",
						"class" : "btn btn-primary btn-xs",
						click: function() {
							$( this ).dialog( "close" ); 
							window.open(anchor.href);
						} 
					}
				]
			});
		});
		$(document).ready(function() {
			$('#overall_config').addClass('active open');
			$('#routerConfigUpdate').addClass('active');
			var state = '${model.opState}';
			if(state=='Success'){
				$('#state').html('操作成功');
			}else{
				$('#state').html('操作失败');
			}
			setInterval(function(){
				$('#state').html('&nbsp;');
			},3000);
		});
	</script>