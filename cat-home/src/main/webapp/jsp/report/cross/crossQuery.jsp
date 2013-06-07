<%@ page contentType="text/html; charset=utf-8"%>
<script>
	function query(){
		var reportType = '${payload.reportType}';
		var domain='${model.domain}';
		var date='${model.date}';
		var method = $('#method').val();
		
		window.location.href="?op=query&domain="+domain+"&date="+date+"&reportType="+reportType+"&method="+method;
	}
</script>
<table>
	<tr>
	<th><span class='text-error'>查询当前这个报表时间段内，一个方法被哪些应用调用</span>
	<input type="text" class='input-xxlarge' id="method" size="100" value="${payload.method}"></input>
	<input type="submit" class='btn btn-primary btn-small' onClick="query()"></input></th></tr>
</table>

