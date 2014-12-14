	<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<div class="row-fluid">
			<div class="tabbable "  > <!-- Only required for left/right tabs -->
			<ul class="nav nav-tabs" style="height:50px">
			 	<li class="text-right active"><a id="tab1Href" href="#tab1" data-toggle="tab"><strong>Web</strong></a></li>
			 	<li class="text-right "><a id="tab2Href" href="#tab2" data-toggle="tab"><strong>Service</strong></a></li>
			</ul>
			<div class="tab-content">
				<div class="tab-pane active" id="tab1">
					<div class="report"  style="display:inline-flex;">
						<table id="web_content" class="table table-striped table-condensed   table-hover">
							<thead>
							<tr>
								<th>Web应用</th>
								<th>CMDB</th>
								<th>机器数</th>
								<th>访问量<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left"  data-content="一段时间【小时、天、周、月】的URL访问总量"></i></th>
								<th>集群QPS<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left"  data-content="一段时间【小时、天、周、月】集群机器URL每秒的访问最大量"></i></th>
								<th>单机QPS<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left"  data-content="一段时间【小时、天、周、月】单台机器URL每秒的访问最大量"></i></th>
								<th>错误量</th>
								<th>错误量%</th>
								<th>响应时间(ms)</th>
								<th>95Line(ms)<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left"  data-content="一段时间【小时、天、周、月】内URL响应时间的95线"></i></th>
								<th>Load(平均)<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left"  data-content="一段时间【小时、天、周、月】内所有机器的load平均值"></i></th>
								<th>Load(最大)<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left"  data-content="一段时间【小时、天、周、月】内所有机器load的最大值"></i></th>
								<th>FullGc(小时平均)<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left"  data-content="所有机器一段时间【小时、天、周、月】内fullGc的平均数量"></i></th>
								<th>FullGc(最大)<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left"  data-content="一段时间【小时、天、周、月】内单台机器fullGc的最大数量"></i></th>
							</tr></thead>
						<tbody>
							<c:forEach var="item" items="${model.utilizationWebList}" varStatus="status">
								<tr>
									<td>${item.id}</td>
									<td>${item.cmdbId}</td>
									<td style="text-align:right">${item.machineNumber}</td>
									<td style="text-align:right">${w:format(item.applicationStates.URL.count,'###0')}</td>
									<td style="text-align:right">${w:format(item.applicationStates.URL.maxQps,'###0')}</td>
									<td style="text-align:right">${w:format(item.applicationStates.URL.maxQps/item.machineNumber,'###0')}</td>
									<td style="text-align:right">${w:format(item.applicationStates.URL.failureCount,'###0')}</td>
									<td style="text-align:right">${w:format(item.applicationStates.URL.failurePercent,'###%')}</td>
									<td style="text-align:right">${w:format(item.applicationStates.URL.avg,'#0.0')}</td>
									<td style="text-align:right">${w:format(item.applicationStates.URL.avg95,'#0.0')}</td>
									<td style="text-align:right">${w:format(item.machineStates.load.avg,'#0.0')}</td>
									<td style="text-align:right">${w:format(item.machineStates.load.avgMax,'#0.0')}</td>
									<td style="text-align:right">${w:format(item.machineStates.fullGc.avg,'#0.0')}</td>
									<td style="text-align:right">${w:format(item.machineStates.fullGc.avgMax,'#0.0')}</td>
								</tr>
							</c:forEach></tbody>
						</table>
						
					</div>
				</div>
				<div class="tab-pane" id="tab2">
					<div class="report"  style="display:inline-flex;">
						<table id="service_content" class="table table-striped table-condensed   table-hover">
							<thead>
								<tr>
								<th>Service应用</th>
								<th>CMDB</th>
								<th>机器数</th>
								<th>访问量<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left"  data-content="一段时间【小时、天、周、月】的Service访问总量"></i></th>
								<th>集群QPS<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left"  data-content="一段时间【小时、天、周、月】集群机器Service每秒的访问最大量"></i></th>
								<th>单机QPS<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left"  data-content="一段时间【小时、天、周、月】单台机器Service每秒的访问最大量"></i></th>
								<th>错误量</th>
								<th>错误量%</th>
								<th>响应时间</th>
								<th>95Line<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left"  data-content="一段时间【小时、天、周、月】内Service响应时间的95线"></i></th>
								<th>Load(平均)<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left"  data-content="一段时间【小时、天、周、月】内所有机器的load平均值"></i></th>
								<th>Load(最大)<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left"  data-content="一段时间【小时、天、周、月】内所有机器load的最大值"></i></th>
								<th>FullGc(小时平均)<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left"  data-content="所有机器一段时间【小时、天、周、月】内fullGc的平均数量"></i></th>
								<th>FullGc(最大)<i tips="" data-trigger="hover" class="glyphicon glyphicon-question-sign" data-toggle="popover" data-placement="left"  data-content="一段时间【小时、天、周、月】内单台机器fullGc的最大数量"></i></th>
							</tr></thead>
						<tbody>
							<c:forEach var="item" items="${model.utilizationServiceList}" varStatus="status">
								<tr>
									<td>${item.id}</td>
									<td>${item.cmdbId}</td>
									<td style="text-align:right">${item.machineNumber}</td>
									<td style="text-align:right">${w:format(item.applicationStates.PigeonService.count,'###0')}</td>
									<td style="text-align:right">${w:format(item.applicationStates.PigeonService.maxQps,'###0')}</td>
									<td style="text-align:right">${w:format(item.applicationStates.PigeonService.maxQps/item.machineNumber,'###0')}</td>
									<td style="text-align:right">${w:format(item.applicationStates.PigeonService.failureCount,'###0')}</td>
									<td style="text-align:right">${w:format(item.applicationStates.PigeonService.failurePercent,'###%')}</td>
									<td style="text-align:right">${w:format(item.applicationStates.PigeonService.avg,'#0.0')}</td>
									<td style="text-align:right">${w:format(item.applicationStates.PigeonService.avg95,'#0.0')}</td>
									<td style="text-align:right">${w:format(item.machineStates.load.avg,'#0.0')}</td>
									<td style="text-align:right">${w:format(item.machineStates.load.avgMax,'#0.0')}</td>
									<td style="text-align:right">${w:format(item.machineStates.fullGc.avg,'#0.0')}</td>
									<td style="text-align:right">${w:format(item.machineStates.fullGc.avgMax,'#0.0')}</td>
								</tr>
							</c:forEach></tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
</div>