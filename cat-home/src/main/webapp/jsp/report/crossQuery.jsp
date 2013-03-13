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
	<th>查询当前这个报表时间段内，一个方法被哪些应用调用
	<input type="text" id="method" size="100" value="${payload.method}"></input>
	<input type="submit" onClick="query()"></input></th>
</table>

