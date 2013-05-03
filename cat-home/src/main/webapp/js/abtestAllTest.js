$(document).ready(function() {
	$('#ckall').click(function() {
		if ($('#ckall').is(':checked')) {
			$(".table input[type='checkbox']").prop('checked', true);
		} else {
			$(".table input[type='checkbox']").prop('checked', false);
		}
	});
	
	$("#prevNavigation").click(function(){
		//$(".pager .disabled").not("#prevNavigation").not("nextNavigation").next()
	});
});