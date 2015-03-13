<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<a:config>
			<form name="storageGroupConfigUpdate" id="form" method="post"
				action="${model.pageUri}?op=storageGroupConfigUpdate">
				<table class="table table-striped table-condensed   table-hover">
					<tr>
						<td><textarea name="content" style="width:100%" rows="20" cols="150">${model.content}</textarea></td>
					</tr>
					<tr>
						<td style="text-align:center"><input class='btn btn-primary' 
							type="submit" name="submit" value="提交" /></td>
					</tr>
				</table>
			</form>
			<h4 class="text-center text-danger" id="state">&nbsp;</h4>
</a:config>
<script type="text/javascript">
		$(document).ready(function() {
			$('#alert_config').addClass('active open');
			$('#storageGroupConfigUpdate').addClass('active');
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