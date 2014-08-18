<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<a:body>
<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig.js']}" target="head-js" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<div class="row-fluid">
		<div class="span2">
			<%@include file="../configTree.jsp"%>
		</div>
		<div class="span10">
			<form name="thirdPartyConfigUpdate" id="form" method="post"
				action="${model.pageUri}?op=thirdPartyConfigUpdate">
				<h4 class="text-center text-error" id="state">&nbsp;</h4>
				<h4 class="text-center text-error">第三方监控配置</h4>
				
				<table class="table table-striped table-bordered table-condensed table-hover">
					<tr>
						<td style="width:60%">
						<textarea name="content" style="width:100%" rows="20" cols="150">${model.content}</textarea>
						</td>
						<td style="width:40%">
						<h4>1. HTTP </h4>
						<p><span class="text-error">[url]</span>：监控的对象</p>
						<p><span class="text-error">[type]</span>：<span class="text-error">get</span> 或 <span class="text-error">post</span></p>
						<p><span class="text-error">[domain]</span>：依赖于该第三方的项目名，会向该项目组联系人发第三方告警</p>
						<p><span class="text-error">[par]</span>：请求中包含的参数，<span class="text-error">id</span>为参数名称，<span class="text-error">value</span>为参数值</p>
						<p>例如：
<xmp style="width:auto"><http url="http://cat.dp:8080" type="get" domain="Cat">
  <par id="domain" value="Cat"/>
  <par id="date" value="2014073111"/>
</http>
</xmp>
						</p>
						<br/>所有标红部分均为小写。
						
						</td>
					</tr>
					<tr>
						<td style="text-align:center"><input class='btn btn-primary' 
							type="submit" name="submit" value="提交" /></td>
					</tr>
				</table>
			</form>
		</div>
	</div>
</a:body>
<script type="text/javascript">
		$(document).ready(function() {
			$('#thirdPartyConfigUpdate').addClass('active');
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