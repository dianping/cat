<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<div class="tabbable "  > <!-- Only required for left/right tabs -->
	<ul class="nav nav-tabs" style="height:60px">
	 	<li class="text-right active"><a id="tab1Href" href="#tab1" data-toggle="tab"><strong>远程调用最多【URL】</strong></a></li>
	 	<li class="text-right"><a id="tab2Href" href="#tab2" data-toggle="tab"><strong>远程调用最多【Service】</strong></a></li>
	 	<li class="text-right"><a id="tab1Href" href="#tab3" data-toggle="tab"><strong>数据库访问最多【URL】</strong></a></li>
	 	<li class="text-right"><a id="tab2Href" href="#tab4" data-toggle="tab"><strong>数据库访问最多【Service】</strong></a></li>
	 	<li class="text-right"><a id="tab1Href" href="#tab5" data-toggle="tab"><strong>缓存访问最多【URL】</strong></a></li>
	 	<li class="text-right"><a id="tab2Href" href="#tab6" data-toggle="tab"><strong>缓存访问最多【Service】</strong></a></li>
	</ul>
	<div class="tab-content">
			<div class="tab-pane active" id="tab1">
				<table class="table table-striped table-condensed   table-hover">
					<tr>
						<th width="10%">项目名</th>
						<th width="70%">URL</th>
						<th width="10%">链接</th>
						<th width="10%" style="text-align:right">访问次数</th>
					</tr>
					<c:forEach var="item" items="${model.callUrls}" varStatus="status">
						<tr>
							<td>${item.domain}</td>
							<td>${item.name}</td>
							<td><a href="/cat/r/m/${item.logview}?domain=${model.domain}">样本链接</a></td>
							<td style="text-align:right">${item.count}</td>
						</tr>
					</c:forEach>
				</table>
			</div>
			<div class="tab-pane" id="tab2">
				<table class="table table-striped table-condensed   table-hover">
						<tr>
							<th width="10%">项目名</th>
							<th width="70%">Service</th>
							<th width="10%">链接</th>
							<th width="10%" style="text-align:right">访问次数</th>
						</tr>
					<c:forEach var="item" items="${model.callServices}" varStatus="status">
						<tr>
							<td>${item.domain}</td>
							<td>${item.name}</td>
							<td><a href="/cat/r/m/${item.logview}?domain=${model.domain}">样本链接</a></td>
							<td style="text-align:right">${item.count}</td>
						</tr>
					</c:forEach>
				</table>
			</div>
			<div class="tab-pane" id="tab3">
				<table class="table table-striped table-condensed   table-hover">
						<tr>
							<th width="10%">项目名</th>
							<th width="70%">URL</th>
							<th width="10%">链接</th>
							<th width="10%" style="text-align:right">访问次数</th>
						</tr>
					<c:forEach var="item" items="${model.sqlUrls}" varStatus="status">
						<tr>
							<td>${item.domain}</td>
							<td>${item.name}</td>
							<td><a href="/cat/r/m/${item.logview}?domain=${model.domain}">样本链接</a></td>
							<td style="text-align:right">${item.count}</td>
						</tr>
					</c:forEach>
				</table>
			</div>
			<div class="tab-pane" id="tab4">
			<table class="table table-striped table-condensed   table-hover">
						<tr>
							<th width="10%">项目名</th>
							<th width="70%">URL</th>
							<th width="10%">链接</th>
							<th width="10%" style="text-align:right">访问次数</th>
						</tr>
					<c:forEach var="item" items="${model.sqlServices}" varStatus="status">
						<tr>
							<td>${item.domain}</td>
							<td>${item.name}</td>
							<td><a href="/cat/r/m/${item.logview}?domain=${model.domain}">样本链接</a></td>
							<td style="text-align:right">${item.count}</td>
						</tr>
					</c:forEach>
				</table>
			</div>
			<div class="tab-pane" id="tab5">
			<table class="table table-striped table-condensed   table-hover">
						<tr>
							<th width="10%">项目名</th>
							<th width="70%">URL</th>
							<th width="10%">链接</th>
							<th width="10%" style="text-align:right">访问次数</th>
						</tr>
					<c:forEach var="item" items="${model.cacheUrls}" varStatus="status">
						<tr>
							<td>${item.domain}</td>
							<td>${item.name}</td>
							<td><a href="/cat/r/m/${item.logview}?domain=${model.domain}">样本链接</a></td>
							<td style="text-align:right">${item.count}</td>
						</tr>
					</c:forEach>
				</table>
			</div>
			<div class="tab-pane" id="tab6">
			<table class="table table-striped table-condensed   table-hover">
						<tr>
							<th width="10%">项目名</th>
							<th width="70%">URL</th>
							<th width="10%">链接</th>
							<th width="10%" style="text-align:right">访问次数</th>
						</tr>
					<c:forEach var="item" items="${model.cacheServices}" varStatus="status">
						<tr>
							<td>${item.domain}</td>
							<td>${item.name}</td>
							<td><a href="/cat/r/m/${item.logview}?domain=${model.domain}">样本链接</a></td>
							<td style="text-align:right">${item.count}</td>
						</tr>
					</c:forEach>
				</table>
			</div>
		</div>
</div>
