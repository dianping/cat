function exceptionValidate(){
	$("#form").validate({
		rules : {
			"exceptionLimit.domain" : {
				required : true
			},
			"exceptionLimit.id" : {
				required : true
			},
			"exceptionLimit.warning" : {
				required : true,
				digits:true
			},
			"exceptionLimit.errorThreshold" : {
				required : true,
				digits:true
			}
		},
		messages : {
			"exceptionLimit.domain" : {
				required : "输入域名"
			},
			"exceptionLimit.id" : {
				required : "输入异常名称"
			},
			"exceptionLimit.warning" : {
				required : "输入阈值",
				digits : "输入合法整数"
			},
			"exceptionLimit.error" : {
				required : "输入阈值",
				digits : "输入合法整数"
			}
		}
	});
}

function edgeValidate(){
	$("#form").validate({
		rules : {
			"edgeConfig.from" : {
				required : true
			},
			"edgeConfig.to" : {
				required : true
			},
			"edgeConfig.warningThreshold" : {
				required : true,
				digits:true
			},
			"edgeConfig.errorThreshold" : {
				required : true,
				digits:true
			},
			"edgeConfig.warningResponseTime" : {
				required : true,
				number :true
			},
			"edgeConfig.errorResponseTime" : {
				required : true,
				number :true
			}
		},
		messages : {
			"edgeConfig.from" : {
				required : "请输入项目"
			},
			"edgeConfig.to" : {
				required : "请输入被调用项目"
			},
			"edgeConfig.warningThreshold" : {
				required : "输入阈值",
				digits : "输入合法整数"
			},
			"edgeConfig.errorThreshold" : {
				required : "输入阈值",
				digits : "输入合法整数"
			},
			"edgeConfig.warningResponseTime" : {
				required : "输入阈值",
				number : "输入合法数字"
			},
			"edgeConfig.errorResponseTime" : {
				required : "输入阈值",
				number : "输入合法数字"
			}
		}
	});
}

function metricValidate(){
	$("#form").validate({
		rules : {
			"metricItemConfig.domain" : {
				required : true
			},
			"metricItemConfig.type" : {
				required : true
			},
			"metricItemConfig.metricKey" : {
				required : true
			},
			"metricItemConfig.title" : {
				required : true
			},
			"metricItemConfig.viewOrder" : {
				required : true,
				number :true
			}
		},
		messages : {
			"metricItemConfig.domain" : {
				required : "请选择项目"
			},
			"metricItemConfig.type" : {
				required :  "请选择类型"
			},
			"metricItemConfig.metricKey" : {
				required : "请输入MetricKey，URL或者Service的二级分类，比如/index等"
			},
			"metricItemConfig.title" : {
				required : "请输入显示标题"
			},
			"metricItemConfig.viewOrder" : {
				required : "请输入显示顺序",
				number :"请输入double数字"
			}
		}
	});
}


function nodeValidate(){
	$("#form").validate({
		rules : {
			"domainConfig.id" : {
				required : true
			},
			"domainConfig.warningThreshold" : {
				required : true,
				digits:true
			},
			"domainConfig.errorThreshold" : {
				required : true,
				digits:true
			},
			"domainConfig.warningResponseTime" : {
				required : true,
				number :true
			},
			"domainConfig.errorResponseTime" : {
				required : true,
				number :true
			}
		},
		messages : {
			"domainConfig.id" : {
				required : "请输入项目"
			},
			"domainConfig.warningThreshold" : {
				required : "输入阈值",
				digits : "输入合法整数"
			},
			"domainConfig.errorThreshold" : {
				required : "输入阈值",
				digits : "输入合法整数"
			},
			"domainConfig.warningResponseTime" : {
				required : "输入阈值",
				number : "输入合法数字"
			},
			"domainConfig.errorResponseTime" : {
				required : "输入阈值",
				number : "输入合法数字"
			}
		}
	});
}