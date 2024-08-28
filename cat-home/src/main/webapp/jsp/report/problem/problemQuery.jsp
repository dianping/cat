<%@ page contentType="text/html; charset=utf-8"%>
<div class="text-left" style="margin-top: 5px">&nbsp;
		Long-url &nbsp;<select class="input-small" size="1" id="p_longUrl">
				${model.defaultThreshold}
				<option value="500">0.5 Sec</option>
				<option value="1000">1.0 Sec</option>
				<option value="1500">1.5 Sec</option>
				<option value="2000">2.0 Sec</option>
				<option value="3000">3.0 Sec</option>
				<option value="5000">5.0 Sec</option>
		</select>&nbsp;&nbsp;&nbsp;
		Long-sql &nbsp;<select size="1" id="p_longSql" class="input-small">
				${model.defaultSqlThreshold}
				<option value="100">100 ms</option>
				<option value="500">500 ms</option>
				<option value="1000">1000 ms</option>
				<option value="3000">3000 ms</option>
				<option value="5000">5000 ms</option>
		</select>&nbsp;&nbsp;&nbsp;
		Long-cache &nbsp;<select size="1" id="p_longCache" class="input-small">
			<option value="10">10 ms</option>
			<option value="50">50 ms</option>
			<option value="100">100 ms</option>
			<option value="500">500 ms</option>
		</select>&nbsp;&nbsp;&nbsp;
		Long-rpc-call &nbsp;<select size="1" id="p_longCall" class="input-small">
			<option value="50">50 ms</option>
			<option value="100">100 ms</option>
			<option value="500">500 ms</option>
			<option value="1000">1000 ms</option>
			<option value="3000">3000 ms</option>
			<option value="5000">5000 ms</option>
		</select>&nbsp;&nbsp;
		Long-rpc-service &nbsp;<select size="1" id="p_longService" class="input-small">
				${model.defaultSqlThreshold}
				<option value="50">50 ms</option>
				<option value="100">100 ms</option>
				<option value="500">500 ms</option>
				<option value="1000">1000 ms</option>
				<option value="3000">3000 ms</option>
				<option value="5000">5000 ms</option>
		</select>&nbsp;&nbsp;&nbsp;
		<input class="btn btn-primary btn-sm"  value="查询"
			onclick="longTimeChange('${model.date}','${model.domain}','${model.ipAddress}')"
			type="submit"></div>

<script>
	var urlThreshold='${payload.urlThreshold}';
	$("#p_longUrl").val(urlThreshold) ;

	var sqlThreshold='${payload.sqlThreshold}';
	$("#p_longSql").val(sqlThreshold) ;

	var serviceThreshold='${payload.serviceThreshold}';
	$("#p_longService").val(serviceThreshold) ;

	var cacheThreshold='${payload.cacheThreshold}';
	$("#p_longCache").val(cacheThreshold) ;

	var callThreshold='${payload.callThreshold}';
	$("#p_longCall").val(callThreshold) ;
</script>
