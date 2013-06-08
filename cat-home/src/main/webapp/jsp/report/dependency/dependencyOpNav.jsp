<%@ page contentType="text/html; charset=utf-8" %>
<div class="row-fluid">
	<div class="span9 text-center">
		<a class="btn btn-danger  btn-primary" href="?minute=${model.minute}&domain=${model.domain}&date=${model.date}">实时趋势图</a>
		<a class="btn btn-danger  btn-primary" href="?op=dependencyGraph&minute=${model.minute}&domain=${model.domain}&date=${model.date}">实时拓扑图</a>
		<a class="btn btn-danger  btn-primary" href="?op=productLine&minute=${model.minute}&domain=${model.domain}&date=${model.date}">产品线监控</a>
		<a class="btn btn-danger  btn-primary" href="?op=dashboard&minute=${model.minute}&domain=${model.domain}&date=${model.date}">应用监控大盘</a>
	</div>
	<div class="span3 text-center">
		<div class='text-center'>
			<a class='btn btn-small btn-primary' href="?op=${payload.action.name}&refresh=true&frequency=10&domain=${model.domain}">10秒定时刷新</a>
			<a class='btn btn-small btn-primary' href="?op=${payload.action.name}&refresh=true&frequency=20&domain=${model.domain}">20秒定时刷新</a>
			<a class='btn btn-small btn-primary' href="?op=${payload.action.name}&refresh=true&frequency=30&domain=${model.domain}">30秒定时刷新</a>
		</div>
	</div>
</div>
<script type="text/javascript">
$(document).ready(function() {
	var refresh = ${payload.refresh};
	var frequency = ${payload.frequency};
	if(refresh){
		setInterval(function(){
			location.reload();				
		},frequency*1000);
	}
});
</script>