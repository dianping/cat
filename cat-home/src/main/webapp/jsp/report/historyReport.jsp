<script>
	var domain='${model.domain}';
	var type = '${model.action.name}';
</script>
<table>
<tr>
		<th>
			Domain:
			<select id="domain">
				<option value="MobileApi">MobileApi</option>
				<option value="TuangouApi">TuangouApi</option>
				<option value="Cat">Cat</option>
			</select>
		</th>
		<th>
			Report Type:
			<select id="reportType">
				<option value="transaction">Transaction</option>
				<option value="event">Event</option>
				<option value="problem">Problem</option>
			</select>
		</th>
		<th>
			Date Type:
			<select size="1" id="id_dateType">
				<option value="day">Day</option>
				<option value="week">Week</option>
				<option value="month">Month</option>
			</select>
			StartTime<input type="text" id="startDate" size="10"
					onchange="onStartDateChange()" value="${payload.startDate}">
			EndTime  <input type="text" id="endDate" size="10" value="${payload.endDate}">
		</th>
		<th>
			<input value="Go" onclick="showSummarizedReport()" type="submit">
		</th>
</tr>
</table>