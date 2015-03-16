<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<a:config>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}"
		target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig.js']}"
		target="head-js" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />

			<form name="alertDefaultReceivers" id="form" method="post"
				action="${model.pageUri}?op=alertDefaultReceivers"
				onsubmit="return validate_form(this)">
				<table
					class="table table-striped table-condensed   table-hover">
					<tr>
						<td><textarea name="content" style="width: 100%" rows="20"
								cols="150">${model.content}</textarea></td>
					</tr>
					<input name="allOnOrOff" id="allOnOrOff" type="text" value=""
						style="display:none"></input>
					<tr>
						<td style="text-align: center"><input class='btn btn-primary'
							type="submit" name="submit" id="submit" value="提交" />&nbsp;&nbsp;<input
							class='btn btn-primary' id="allOn" value="全部告警" />&nbsp;&nbsp;<input
							class='btn btn-primary' id="allOff" value="暂停告警" /></td>
					</tr>
				</table>
								<h4 class="text-center text-danger" id="state">&nbsp;</h4>
			</form>
</a:config>
<script type="text/javascript">
	$(document).ready(function() {
		$('#overall_config').addClass('active open');
		$('#alertDefaultReceivers').addClass('active');
		var state = '${model.opState}';
		if (state == 'Success') {
			$('#state').html('操作成功');
		} else {
			$('#state').html('操作失败');
		}
		setInterval(function() {
			$('#state').html('&nbsp;');
		}, 3000);
		$('#allOn').click(function() {
			$('#allOnOrOff').attr("value","on");
			$('#submit').click();
		})
		$('#allOff').click(function() {
			$('#allOnOrOff').attr("value","off");
			$('#submit').click();
		})
	});
</script>