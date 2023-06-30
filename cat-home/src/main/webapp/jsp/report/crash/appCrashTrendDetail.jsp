<%@ page contentType="text/html; charset=utf-8" %>
<style>
	.input-group {
		margin: 7px 3px;
		height: 34px;
	}

	select {
		height: 34px;
	}
</style>
<table style="width:100%;margin-left: 7px">
	<tr>
		<th align=left>
			<div class="input-group" style="float:left;width:130px">
				<span class="input-group-addon">开始时间</span>
				<input type="text" id="startTime" style="width:130px;height: 34px"/>
			</div>

			<div class="input-group" style="float:left;width:85px">
				<span class="input-group-addon">结束时间</span>
				<input type="text" id="endTime" style="width:85px;height: 34px"/>
			</div>
			<div class="input-group" style="float:left;width: 120px;">
				<span class="input-group-addon">App 名称</span>
				<select id="appName" style="width: 120px;">
					<c:forEach var="appName" items="${model.crashLogDisplayInfo.appNames}">
						<option value="${appName.id}">${appName.value}</option>
					</c:forEach>
				</select>
			</div>
			<div class="input-group" style="float:left;width: 120px;">
				<span class="input-group-addon">App 版本</span>
				<select id="appVersion" style="width: 120px;">
					<option value="">All</option>
					<c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.appVersions}"
							   varStatus="status">
						<option value="${item}">${item}</option>
					</c:forEach>
				</select>
			</div>
			<div class="input-group" style="float:left;width: 110px;">
				<span class="input-group-addon">模块</span>
				<select id="modules" style="width: 150px;">
					<option value="">All</option>
					<c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.modules}" varStatus="status">
						<option value="${item}">${item}</option>
					</c:forEach>
				</select>
			</div>
		</th>
	</tr>
	<tr>
		<th align=left>
			<div class="input-group" style="float:left;width: 100px;">
				<span class="input-group-addon">平台</span>
				<select id="platform" style="width: 158px;">
					<option value="1">Android</option>
					<option value="2">iOS</option>
					<option value="3">H5</option>
				</select>
			</div>

			<div class="input-group" style="float:left;width: 110px;">
				<span class="input-group-addon">平台版本</span>
				<select id="platVersion" style="width: 86px;">
					<option value="">All</option>
					<c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.platVersions}"
							   varStatus="status">
						<option value="${item}">${item}</option>
					</c:forEach>
				</select>
			</div>

			<input class="btn btn-primary btn-sm" style="margin: 7px 7px;height: 34px"
				   value="查询" onclick="query()"
				   type="submit"/>
			<input class="btn btn-primary" id="checkbox"  onclick="check()" type="checkbox"/>
			     <label for="checkbox"  style="display: -webkit-inline-box;margin-left: 7px">选择对比</label>
		</th>
	</tr>
</table>
<table id="history" style="display: none;width:100%;margin-left: 7px">
	<tr>
		<th align=left>
			<div class="input-group" style="float:left;width:130px">
				<span class="input-group-addon">开始时间</span>
				<input type="text" id="startTime2" style="width:130px;height: 34px"/>
			</div>

			<div class="input-group" style="float:left;width:80px">
				<span class="input-group-addon">结束时间</span>
				<input type="text" id="endTime2" style="width:85px;height: 34px"/>
			</div>

			<div class="input-group" style="float:left;width: 120px;">
				<span class="input-group-addon">App 名称</span>
				<select id="appName2" style="width: 120px;">
					<c:forEach var="appName" items="${model.crashLogDisplayInfo.appNames}">
						<option value="${appName.id}">${appName.value}</option>
					</c:forEach>
				</select>
			</div>

			<div class="input-group" style="float:left;width: 120px;">
				<span class="input-group-addon">App 版本</span>
				<select id="appVersion2" style="width: 120px;">
					<option value="">All</option>
					<c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.appVersions}"
							   varStatus="status">
						<option value="${item}">${item}</option>
					</c:forEach>
				</select>
			</div>

			<div class="input-group" style="float:left;width: 110px;">
				<span class="input-group-addon">模块</span>
				<select id="modules2" style="width: 150px;">
					<option value="">All</option>
					<c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.modules}" varStatus="status">
						<option value="${item}">${item}</option>
					</c:forEach>
				</select>
			</div>
		</th>
	</tr>
	<tr>
		<th align=left>
			<div class="input-group" style="float:left;width: 158px;">
				<span class="input-group-addon">平台</span>
				<select id="platform2" style="width: 158px;">
					<option value="1">Android</option>
					<option value="2">iOS</option>
					<option value="3">H5</option>
				</select>
			</div>

			<div class="input-group" style="width: 86px;float:left;">
				<span class="input-group-addon">平台版本</span>
				<select id="platVersion2" style="width: 86px;">
					<option value="">All</option>
					<c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.platVersions}"
							   varStatus="status">
						<option value="${item}">${item}</option>
					</c:forEach>
				</select>
			</div>
		</th>
	</tr>
</table>

<div style="float: left; width: 100%;">
	<div id="${model.crashLogDisplayInfo.lineChart.id}"></div>
</div>
