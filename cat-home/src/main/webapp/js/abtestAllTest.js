$(document).ready(function() {
	$('#ckall').click(function() {
		if ($('#ckall').is(':checked')) {
			$(".table input[type='checkbox']").prop('checked', true);
		} else {
			$(".table input[type='checkbox']").prop('checked', false);
		}
	});

	$("#prevNavigation").click(function() {
		// $(".pager
		// .disabled").not("#prevNavigation").not("nextNavigation").next()
	});
});

(function(w) {
	var abtest = {
		"httpError" : function(xhr, textStatus, errorThrown) {
			// 去掉按钮disable
			abtest.alertError('抱歉啦', '抱歉，网络发生错误了，请刷新页面试试...');
		},
		"alertError" : function(title, errorMsg) {
			// 显示错误消息
			$('#errorMsgModal > div[class="modal-header"] > h3').text(title);
			$('#errorMsgModal > div[class="modal-body"] > p').text(errorMsg);
			$('#errorMsgModal').modal('show');
		}
	};
	w.abtest = abtest;
}(window || this));