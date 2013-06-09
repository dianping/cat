<%@ page contentType="text/html; charset=utf-8" %>
<div class="row-fluid" style="margin-top:2px;">
	<div class="span9 text-center">
		<a id="navlineChart" class="btn btn-primary" href="?minute=${model.minute}&domain=${model.domain}&date=${model.date}">实时趋势图</a>
		<a id="navdependencyGraph" class="btn btn-primary" href="?op=dependencyGraph&minute=${model.minute}&domain=${model.domain}&date=${model.date}">实时拓扑图</a>
		<a id="navproductLine" class="btn btn-primary" href="?op=productLine&minute=${model.minute}&domain=${model.domain}&date=${model.date}">产品线监控</a>
		<a id="navdashboard" class="btn btn-primary" href="?op=dashboard&minute=${model.minute}&domain=${model.domain}&date=${model.date}">监控仪表盘</a>
	</div>
	<div class="span3 text-center">
		<div class='text-center'>
			<a id="refresh10" class='btn btn-small btn-primary' href="?op=${payload.action.name}&refresh=true&frequency=10&domain=${model.domain}">10秒定时刷新</a>
			<a id="refresh20" class='btn btn-small btn-primary' href="?op=${payload.action.name}&refresh=true&frequency=20&domain=${model.domain}">20秒定时刷新</a>
			<a id="refresh30" class='btn btn-small btn-primary' href="?op=${payload.action.name}&refresh=true&frequency=30&domain=${model.domain}">30秒定时刷新</a>
		</div>
	</div>
</div>
<script type="text/javascript">
$(document).ready(function() {
	var id = '${payload.action.name}';
	$('#nav'+id).addClass('btn-danger');
	
	var refresh = ${payload.refresh};
	var frequency = ${payload.frequency};
	if(refresh){
		$('#refresh${payload.frequency}').addClass('btn-danger');
		setInterval(function(){
			location.reload();				
		},frequency*1000);
	};
	
});
</script>