<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!-- Modal -->
<div id="myModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
</div>
 <div class="row-fluid">
 		<div class="span6">
 			<h5 class="text-danger text-center">项目本身详细数据</h5>
 			<table	class="contents table table-striped table-condensed  ">
			<thead>	<tr>
				<th>Name</th>
				<th>Total</th>
				<th>Failure</th>
				<th>Failure%</th>
				<th>Avg(ms)</th>
				<th>Config</th>
			</tr></thead><tbody>
			<c:forEach var="item" items="${model.segment.indexs}" varStatus="status">
				 <c:set var="itemKey" value="${item.key}" />
				 <c:set var="itemValue" value="${item.value}" />
				<tr>
					<td>${itemValue.name}</td>
					<td style="text-align:right;">${itemValue.totalCount}</td>
					<td style="text-align:right;">${itemValue.errorCount}</td>
					<td style="text-align:right;">${w:format(itemValue.errorCount/itemValue.totalCount,'0.0000')}</td>
					<td style="text-align:right;">${w:format(itemValue.avg,'0.0')}</td>
					<td><a class="nodeConfigUpdate btn btn-primary btn-sm" target="_blank" href="/cat/s/config?op=topologyGraphNodeConfigAdd&type=${itemValue.name}&domain=${model.domain}">配置阀值</a></td>
				</tr>		
			</c:forEach></tbody>
		</table>
 		</div>
 		<div class="span6">
 			<h5 class="text-danger text-center">依赖项目详细数据</h5>
		<table class="contentsDependency table table-striped table-condensed  ">
			<thead>	<tr>
				<th>Type</th>
				<th>Target</th>
				<th>Total</th>
				<th>Failure</th>
				<th>Failure%</th>
				<th>Avg(ms)</th>
				<th>Config</th>
			</tr></thead><tbody>
			<c:forEach var="item" items="${model.segment.dependencies}" varStatus="status">
				 <c:set var="itemKey" value="${item.key}" />
				 <c:set var="itemValue" value="${item.value}" />
				<tr>
					<td>${itemValue.type}</td>
					<td>${itemValue.target}</td>
					<td style="text-align:right;">${itemValue.totalCount}</td>
					<td style="text-align:right;">${itemValue.errorCount}</td>
					<td style="text-align:right;">${w:format(itemValue.errorCount/itemValue.totalCount,'0.0000')}</td>
					<td style="text-align:right;">${w:format(itemValue.avg,'0.0')}</td>
					<td>
					<c:choose>
						<c:when test="${itemValue.type eq 'PigeonServer' || itemValue.type eq 'PigeonService'}">
							<a class="btn btn-primary edgeConfigUpdate btn-sm" target="_blank" href="/cat/s/config?op=topologyGraphEdgeConfigAdd&type=PigeonCall&to=${model.domain}&from=${itemValue.target}">配置阀值</a>
						</c:when>
						<c:otherwise>
							<a class="btn btn-primary edgeConfigUpdate btn-sm" target="_blank" href="/cat/s/config?op=topologyGraphEdgeConfigAdd&type=${itemValue.type}&from=${model.domain}&to=${itemValue.target}">配置阀值</a>
						</c:otherwise>
					</c:choose>
					</td>
				</tr>		
			</c:forEach></tbody>
		</table>	  			
 		</div>
 </div>
 <script>
	$(document).delegate('.nodeConfigUpdate', 'click', function(e){
		var anchor = this,
			el = $(anchor);
		
		if(e.ctrlKey || e.metaKey){
			return true;
		}else{
			e.preventDefault();
		}
		$.ajax({
			type: "get",
			url: anchor.href,
			success : function(response, textStatus) {
				$('#myModal').html(response);
				$('#myModal').modal();
				nodeValidate();
				 $('#addOrUpdateNodeSubmit').bind("click",function(event){
						event.preventDefault();
						var data =  "type="+$('#type').val()+"&domainConfig.id="+$('#id').val()
						+"&domainConfig.warningThreshold="+$('#warningThreshold').val()+"&domainConfig.errorThreshold="+$('#errorThreshold').val()
						+"&domainConfig.warningResponseTime="+$('#warningResponseTime').val()+"&domainConfig.errorResponseTime="+$('#errorResponseTime').val();
						$.ajax({
							type: "get",
							url: "/cat/s/config?op=topologyGraphNodeConfigAddSumbit",
							data: data,
							success : function(response, textStatus) {
								$('#myModal').modal('hide');
							},
							error : function(ajaxContext) {
								$('#state').html('操作失败，请检查参数正确性！');
						    }
						});
					});
			}
		});
	});
	
	
	$(document).delegate('.edgeConfigUpdate', 'click', function(e){
		var anchor = this,
			el = $(anchor);
		
		if(e.ctrlKey || e.metaKey){
			return true;
		}else{
			e.preventDefault();
		}
		$.ajax({
			type: "get",
			url: anchor.href,
			success : function(response, textStatus) {
				$('#myModal').html(response);
				$('#myModal').modal();
				edgeValidate();
				 $('#addOrUpdateEdgeSubmit').bind("click",function(event){
					event.preventDefault();
					var data =  "edgeConfig.type="+$('#type').val()+"&edgeConfig.from="+$('#from').val()+"&edgeConfig.to="+$('#to').val()
					+"&edgeConfig.warningThreshold="+$('#warningThreshold').val()+"&edgeConfig.errorThreshold="+$('#errorThreshold').val()
					+"&edgeConfig.warningResponseTime="+$('#warningResponseTime').val()+"&edgeConfig.errorResponseTime="+$('#errorResponseTime').val();
					console.log(data);
					$.ajax({
						type: "get",
						url: "/cat/s/config?op=topologyGraphEdgeConfigAddSumbit",
						data: data,
						success : function(response, textStatus) {
							$('#myModal').modal('hide')
						},
						error : function(ajaxContext) {
							$('#state').html('操作失败，请检查参数正确性！');
					    }
					});
				});
			}
		});
	});
 </script>