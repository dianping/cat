<%@ page contentType="text/html; charset=utf-8" %>
<div class="col-xs-12"><div class="row" style="margin-top:2px;">
	<div class='text-right'>
		<a id="refresh10" class='btn btn-sm btn-primary' href="?op=${payload.action.name}&tag=${payload.tag}&product=${payload.product}&group=${payload.group}&fullScreen=${payload.fullScreen}&hideNav=${payload.hideNav}&refresh=true&frequency=10&domain=${model.domain}&timeRange=${payload.timeRange}">10秒</a>
		<a id="refresh20" class='btn btn-sm btn-primary' href="?op=${payload.action.name}&tag=${payload.tag}&product=${payload.product}&group=${payload.group}&fullScreen=${payload.fullScreen}&hideNav=${payload.hideNav}&refresh=true&frequency=20&domain=${model.domain}&timeRange=${payload.timeRange}">20秒</a>
		<a id="refresh30" class='btn btn-sm btn-primary' href="?op=${payload.action.name}&tag=${payload.tag}&product=${payload.product}&group=${payload.group}&fullScreen=${payload.fullScreen}&hideNav=${payload.hideNav}&refresh=true&frequency=30&domain=${model.domain}&timeRange=${payload.timeRange}">30秒</a>
		<a id="fullScreen"  class='btn btn-sm btn-primary' href="?op=${payload.action.name}&tag=${payload.tag}&product=${payload.product}&group=${payload.group}&fullScreen=${!payload.fullScreen}&hideNav=${payload.hideNav}&refresh=${payload.refresh}&frequency=${payload.frequency}&domain=${model.domain}&timeRange=${payload.timeRange}">全屏</a>
	</div>
</div></div>
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