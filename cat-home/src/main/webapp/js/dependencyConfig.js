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