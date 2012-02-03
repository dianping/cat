var tabledata = [];
$(function(){
	var types = data["types"];
	for (i in types) {
		var type = types[i];
		var sampleurl = type.failMessageUrl != null ? "<a href=\"m/"+type.failMessageUrl+"\">fail</a>" : "<a href=\"m/"+type.successMessageUrl+"\">success</a>";	
		var stat = "" + type.min + "/" + type.max + "/" + type.avg + "/" + type.std;
		tabledata.push({"type":type.id, "total":type.totalCount, "fail":type.failCount, "failPercent":type.failPercent, "sample":sampleurl, "stat":stat});
	}
 }
);

domainLink = function(domain, now){
    if(domain == now) {
        return "[ <a style=\"background-color: rgb(255, 204, 0);\" href=\"t?domain="+domain+"\">"+domain+"</a> ]";
    } else {
        return "[ <a href=\"t?domain="+domain+"\">"+domain+"</a> ]";
    }
};

$(function()
    {
        domainlinks = 'Domains: ';
        nowdomain = data["domain"];
        $.each(data.domains, function(i, t){domainlinks += domainLink(t, nowdomain)});
        $("#domainlist").html(domainlinks);
        $("#reporttitle").html("Transaction Report - Domain:" + nowdomain);
        var grid = $("#gridtable");
        grid.jqGrid({
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
                        {name:'sample', index:'sample', sortable:false, align:'center'},                
                        {name:'stat', sortable:false, align:'center',width:200}                
                ],
                sortname:'type',
                sortorder:'asc',
                caption: "",
                height: '100%',
                autowidth: true,
                loadComplete: function() {
                    grid.setGridHeight('auto');
                    //grid.setGridWidth('auto');
                }
        }).navGrid('#pager2',{edit:false,add:false,del:false});
        
        
        for(var i=0;i<=tabledata.length;i++) {
        	grid.jqGrid('addRowData',i+1,tabledata[i]);
        }
        
        $("#gview_gridtable > .ui-jqgrid-titlebar").hide()
        
        //$(function(){
        //    $(window).resize(function(){  
        //          $("#gridTable").setGridWidth($(window).width()*0.99);
         //   });
        //});
	}
);