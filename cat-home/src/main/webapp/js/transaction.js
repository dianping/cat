var tabledata = [];
$(function(){
	var types = data["types"];
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
                colNames:['Type', 'Total Count', 'Fail Count', 'Failure%', 'Sample Link', 'Min/Max/Avg/Std(ms)'],
                colModel:[
                        {name:'type', index:'type', width:200},
                        {name:'total', index:'total', width:100, sorttype:"int", align:'right'},
                        {name:'fail', index:'fail', width:100, sorttype:"int", align:'right'},
                        {name:'failPercent', index:'failPercent', width:100, sorttype:"float", align:'center'},
                        {name:'sample', index:'sample', width:200, sortable:false},                
                        {name:'stat', width:200, sortable:false}                
                ],
                sortname:'type',
                sortorder:'asc',
                caption: "Domain " + data["domain"] + " Transaction Summary",
                height: '100%',
                autowidth: true,
                loadComplete: function() {
                    $("#gridTable").setGridHeight('auto');
                }
        }).navGrid('#pager2',{edit:false,add:false,del:false});
        
        var grid = $("#gridTable");
        for(var i=0;i<=tabledata.length;i++) {
        	grid.jqGrid('addRowData',i+1,tabledata[i]);
        }
        
        $(function(){
            $(window).resize(function(){  
                  $("#gridTable").setGridWidth($(window).width()*0.99);
            });
        });
	}
);