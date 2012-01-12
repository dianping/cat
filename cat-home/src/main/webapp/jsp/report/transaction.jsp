<%@ page contentType="text/html; charset=utf-8" %>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.transaction.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.transaction.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.transaction.Model" scope="request"/>

<!DOCTYPE HTML>
<html>
<head>
<meta charset="utf-8">
<link rel="stylesheet" type="text/css" media="screen" href="../jquery/jquery-ui-1.8.16.custom.css" />
<link rel="stylesheet" type="text/css" media="screen" href="../jquery/ui.jqgrid.css" />
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.min.js"></script>
<script type="text/javascript" src="../jquery/grid.locale-en.js"></script>
<script type="text/javascript" src="../jquery/jquery.jqGrid.min.js"></script>
</head>

<body>
	<script type="text/javascript">
var data =
{
   "transaction-report": ${model.reportInJson}
};

var tabledata = [];
$(function(){
	var types = data["transaction-report"]["type"];
	for (i in types) {
		var type = types[i];
		var sampleid = type.failid == null ? type.successid : type.failid;	
		var stat = "" + type.min + "/" + type.max + "/" + type.avg + "/" + type.std;
		tabledata.push({"type":type.id, "total":type.totalCount, "fail":type.failCount, "failPercent":type.failPercent, "sample":sampleid, "stat":stat});
	}

 }
);
	

$(function()
    {
        $("#gridTable").jqGrid({
                datatype: "local",
                colNames:['Type','TotalCount', 'FailCount', 'Fail%', 'Sample Link', 'M/M/A/µ'],
                colModel:[
                        {name:'type', index:'type'},
                        {name:'total', index:'total', sorttype:"int"},
                        {name:'fail', index:'fail', sorttype:"int"},
                        {name:'failPercent', index:'failPercent', sorttype:"float"},
                        {name:'sample', index:'sample'},                
                        {name:'stat'}                
                                    
                ],
                sortname:'type',
                sortorder:'asc',
                caption: "Transaction Report",
                height: 250,
                loadComplete: function() {
                    var grid = $("#gridTable");
                    var ids = grid.getDataIDs();
                    for (var i = 0; i < ids.length; i++) {
                        grid.setRowData( ids[i], false, {height: 20} );
                    }
                    grid.setGridHeight('auto');
                }
        }).navGrid('#pager2',{edit:false,add:false,del:false});
        for(var i=0;i<=tabledata.length;i++) {
                jQuery("#gridTable").jqGrid('addRowData',i+1,tabledata[i]);
        }
	}
);


</script>

	<table id="gridTable"></table>
	<div id="gridPager"></div>
</body>
</html>