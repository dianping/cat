var tabledata = [];
$(function(){
	var types = data["types"];
	for (i in types) {
		var type = types[i];
		var sampleid = type.failMessageUrl != null ? type.failMessageUrl : type.successMessageUrl;	
		var stat = "" + type.min + "/" + type.max + "/" + type.avg + "/" + type.std;
		tabledata.push({"type":type.id, "total":type.totalCount, "fail":type.failCount, "failPercent":type.failPercent, "sample":"<a href=\"/cat/r/m/"+sampleid+"\" target=\"_blank\">link</a>", "stat":stat});
	}
 }
);

$(function()
    {
        domainlinks = 'Domains ';
        $.each(data.domains, function(i, t){domainlinks += "[ <a href=\"/cat/r/t?domain="+t+"\">"+t+"</a> ]"});
        $("#domainlist").html(domainlinks);
        $("#gridTable").jqGrid({
                defaults : {
                    //shrinkToFit:true,
                    //forceFit:true,
                },
                datatype: "local",
                colNames:['Type', 'Total Count', 'Fail Count', 'Failure%', 'Sample Link', 'Min/Max/Avg/Std(ms)'],
                colModel:[
                        {name:'type', index:'type', align:'center'},
                        {name:'total', index:'total', sorttype:"int", align:'center'},
                        {name:'fail', index:'fail', sorttype:"int", align:'center'},
                        {name:'failPercent', index:'failPercent', sorttype:"float", align:'center'},
                        {name:'sample', index:'sample', sortable:false, align:'center', width:100},                
                        {name:'stat', sortable:false, align:'center',width:200}                
                ],
                sortname:'type',
                sortorder:'asc',
                caption: "Domain " + data["domain"] + " Transaction Summary",
                height: '100%',
                //autowidth: true,
                loadComplete: function() {
                    $("#gridTable").setGridHeight('auto');
                }
        }).navGrid('#pager2',{edit:false,add:false,del:false});
        
        var grid = $("#gridTable");
        for(var i=0;i<=tabledata.length;i++) {
        	grid.jqGrid('addRowData',i+1,tabledata[i]);
        }
        
        //$(function(){
        //    $(window).resize(function(){  
        //          $("#gridTable").setGridWidth($(window).width()*0.99);
         //   });
        //});
	}
);