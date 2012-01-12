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