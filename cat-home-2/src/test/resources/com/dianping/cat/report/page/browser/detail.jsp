	<%@ page session="false" language="java" pageEncoding="UTF-8" %>

<div class="report">
	</br>
	<div>
		<div class="span6">
			<table class="table table-striped table-bordered table-condensed table-hover">
				<tr>
					<th style="text-align:left">Browser</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=browser&sort=browserCount">Count</th>
				</tr>
			
				<c:forEach var="item" items="${model.browserList}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=browserVersion&favor=${item.id}&sort=browserVersionCount">${item.id}</td>
						<td style="text-align:right">${w:format(item.count,'#,###,###,###,##0')}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<div class="span6">
			<div id="browserGraph" class="pieChart"></div>
		</div>	
	</div>	
	<div>
		<div class="span6">
			<table class="table table-striped table-bordered table-condensed table-hover">
				<tr>
					<th style="text-align:left">OS</th>
					<th style="text-align:right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&op=browser&sort=osCount">Count</th>
				</tr>
			
				<c:forEach var="item" items="${model.osList}" varStatus="status">
					<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
						<td>${item.id}</td>
						<td style="text-align:right">${w:format(item.count,'#,###,###,###,##0')}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<div class="span6">
			<div id="osGraph" class="pieChart"></div>
		</div>	
	</div>	
</div>
