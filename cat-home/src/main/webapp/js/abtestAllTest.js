$(document)
		.ready(
				function() {
					$('#ckall').click(
							function() {
								console.log("ddd");
								if ($('#ckall').is(':checked')) {
									$(".table input[type='checkbox']").prop(
											'checked', true);
								} else {
									$(".table input[type='checkbox']").prop(
											'checked', false);
								}
							});
					$("#btnSuspend")
							.click(
									function() {
										var checkbox = $(".table input[type='checkbox']:checked");
										var id = "";
										for ( var i = 0; i < checkbox.length; i++) {
											id = id + $(":nth-child(2)",$(checkbox[i]).closest('tr')).html() + "-";
										}
										url = window.location.href;
										index = url.indexOf("&suspend");
										if (index != -1) {
											window.location.href = url
													.substring(0, index)
													+ "&suspend=-1&ids=" + id;
										} else {
											if (url.indexOf("?") == -1) {
												url = url + '?';
											}
											window.location.href = url
													+ "&suspend=-1&ids=" + id;
										}
										loaction.reload();
									});

					$("#btnResume")
							.click(
									function() {
										var checkbox = $(".table input[type='checkbox']:checked");
										var id = "";
										for ( var i = 0; i < checkbox.length; i++) {
											id = id + $(":nth-child(2)",$(checkbox[i]).closest('tr')).html() + "-";
										}
										url = window.location.href;
										index = url.indexOf("&suspend");
										if (index != -1) {
											window.location.href = url
													.substring(0, index)
													+ "&suspend=1&ids=" + id;
										} else {
											window.location.href = url
													+ "&suspend=1&ids=" + id;
										}
										loaction.reload();
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
