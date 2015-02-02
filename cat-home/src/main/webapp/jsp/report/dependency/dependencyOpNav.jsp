<%@ page contentType="text/html; charset=utf-8" %>
<div  style="margin-top:2px;">
		<div class='text-right'>
			<a id="fullScreen"  class='btn btn-sm btn-primary' href="?op=${payload.action.name}&fullScreen=${!payload.fullScreen}&refresh=${payload.refresh}&hideNav=${payload.hideNav}&frequency=${payload.frequency}&domain=${model.domain}">全屏</a>
		</div>
</div>
<script type="text/javascript">

$(document).ready(function() {
	var id = '${payload.action.name}';
	$('#nav'+id).addClass('btn-danger');
	
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
	
	$('#fullScreen').click(function (e) {
		fullScreen(e);
	});
});
</script>