<%@ page contentType="text/html; charset=utf-8" %>
<div class="text-right">
	<div class='text-right'>
		<a id="fullScreen"  class='btn btn-sm btn-primary' href="?op=${payload.action.name}&product=${payload.product}&group=${payload.group}&fullScreen=${!payload.fullScreen}&hideNav=${payload.hideNav}&refresh=${payload.refresh}&frequency=${payload.frequency}&domain=${model.domain}&timeRange=${payload.timeRange}">全屏</a>
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