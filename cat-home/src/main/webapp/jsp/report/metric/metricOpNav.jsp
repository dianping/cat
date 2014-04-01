<%@ page contentType="text/html; charset=utf-8" %>
<div class="row-fluid" style="margin-top:2px;">
	<div class="span8 text-center">
		<a id="navlineChart" class="btn btn-small btn-primary" href="/cat/r/dependency?domain=${model.domain}&date=${model.date}">实时趋势图</a>
		<a id="navdependencyGraph" class="btn btn-small btn-primary" href="/cat/r/dependency?op=dependencyGraph&domain=${model.domain}&date=${model.date}">实时拓扑图</a>
		<a id="navmetricDashboard" class="btn btn-small btn-primary" href="/cat/r/dependency?op=metricDashboard&domain=${model.domain}&date=${model.date}">系统报错大盘</a>
		<a id="navdashboard" class="btn btn-small btn-primary" href="/cat/r/dependency?op=dashboard&domain=${model.domain}&date=${model.date}">应用监控盘</a>
		<a id="navbussiness" class="btn btn-small btn-primary btn-danger" href="/cat/r/metric?op=dashboard&domain=${model.domain}&date=${model.date}">业务监控大盘</a>
	</div>
	<div class="span4 text-center">
		<div class='text-center'>
			<a id="refresh10" class='btn btn-small btn-primary' href="?op=${payload.action.name}&product=${payload.product}&group=${payload.group}&fullScreen=${payload.fullScreen}&hideNav=${payload.hideNav}&refresh=true&frequency=10&domain=${model.domain}&timeRange=${payload.timeRange}">10秒</a>
			<a id="refresh20" class='btn btn-small btn-primary' href="?op=${payload.action.name}&product=${payload.product}&group=${payload.group}&fullScreen=${payload.fullScreen}&hideNav=${payload.hideNav}&refresh=true&frequency=20&domain=${model.domain}&timeRange=${payload.timeRange}">20秒</a>
			<a id="refresh30" class='btn btn-small btn-primary' href="?op=${payload.action.name}&product=${payload.product}&group=${payload.group}&fullScreen=${payload.fullScreen}&hideNav=${payload.hideNav}&refresh=true&frequency=30&domain=${model.domain}&timeRange=${payload.timeRange}">30秒</a>
			<a id="fullScreen"  class='btn btn-small btn-primary' href="?op=${payload.action.name}&product=${payload.product}&group=${payload.group}&fullScreen=${!payload.fullScreen}&hideNav=${payload.hideNav}&refresh=${payload.refresh}&frequency=${payload.frequency}&domain=${model.domain}&timeRange=${payload.timeRange}">全屏</a>
		</div>
	</div>
</div>
<script type="text/javascript">

$(document).ready(function() {
	var refresh = ${payload.refresh};
	var frequency = ${payload.frequency};
	var fullscreen = ${payload.fullScreen};
	if(fullscreen){
		$('#fullScreen').addClass('btn-danger');
	}
	if(refresh){
		$('#refresh${payload.frequency}').addClass('btn-danger');
		setInterval(function(){
			location.reload();				
		},frequency*1000);
	};
});
</script>