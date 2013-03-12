function init(){
	$("#start" ).datepicker();
	$("#end" ).datepicker();
	$('#contents').dataTable({
		"sPaginationType": "full_numbers",
		'iDisplayLength': 100,
		"oLanguage": {
            "sProcessing": "正在加载中......",
            "sLengthMenu": "每页显示 _MENU_ 条记录",
            "sZeroRecords": "对不起，查询不到相关数据！",
            "sEmptyTable": "表中无数据存在！",
            "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录",
            "sInfoFiltered": "数据表中共为 _MAX_ 条记录",
            "sSearch": "搜索",
            "oPaginate": {
                "sFirst": "首页",
                "sPrevious": "上一页",
                "sNext": "下一页",
                "sLast": "末页"
            }
        }
	});
}

function query(){
	  var queryDomain=$("#domain").val();
	  var queryType=$("#reportType").val();
	  var reportLevel=$("#reportLevel").val();
	  var type=$("#type").val();
	  var name=$("#name").val();
	  var start=$("#start").val();
	  var end=$("#end").val();
	  
	  if(queryDomain==null||queryDomain.trim().length==0){
		  alert("请输入domain名称");
		  return;
	  }
	  if(type==null||type.trim().length==0){
		  alert("请输入查询的Type内容");
		  return;
	  }
	  if(start==null||start.trim().length==0){
		  alert("请输入查询的开始时间");
		  return;
	  }
	  if(end==null||end.trim().length==0){
		  alert("请输入查询的结束时间");
		  return;
	  }
	  
	  window.location.href="?queryDomain="+queryDomain+"&queryType="+queryType+"&reportLevel="+reportLevel+"&type="+type+"&name="+name+'&start='+start+"&end="+end;
	}