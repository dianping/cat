var tabledata = [];
$(function(){
	var types = data["types"];
    var nowdomain = data["domain"];
    if(nowtype != "") {
        var names = types[nowtype]["names"];
        for (i in names) {    
            var name = names[i];
            var sampleurl = name.failMessageUrl != null ? "<a href=\"m/"+name.failMessageUrl+"\">fail</a>" : "<a href=\"m/"+name.successMessageUrl+"\">success</a>";	
            var stat = "" + name.min + "/" + name.max + "/" + name.avg + "/" + name.std;
            tabledata.push({"name":name.id, "total":name.totalCount, "fail":name.failCount, "failPercent":name.failPercent, "sample":sampleurl, "stat":stat});    
        }
    } else {
        for (i in types) {
            var type = types[i];
            var typeurl = "<a href=\"t?domain=" + nowdomain + "&type=" + type.id + "\">" + type.id + "</a>";
            var sampleurl = type.failMessageUrl != null ? "<a href=\"m/"+type.failMessageUrl+"\">fail</a>" : "<a href=\"m/"+type.successMessageUrl+"\">success</a>";	
            var stat = "" + type.min + "/" + type.max + "/" + type.avg + "/" + type.std;
            tabledata.push({"type":typeurl, "total":type.totalCount, "fail":type.failCount, "failPercent":type.failPercent, "sample":sampleurl, "stat":stat});
        }
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
        var domainlinks = 'Domains: ';
        var nowdomain = data["domain"];
        $.each(data.domains, function(i, t){domainlinks += domainLink(t, nowdomain)});
        $("#domainlist").html(domainlinks);
        $("#reporttitle").html("Transaction Report - Domain:" + nowdomain);
        var grid = $("#gridtable");
        var colnames;
        var colmodels;
        if(nowtype == "") {
            colnames = ['Type', 'Total Count', 'Fail Count', 'Failure%', 'Sample Link', 'Min/Max/Avg/Std(ms)'];
            colmodels = [
                        {name:'type', index:'type', align:'center'},
                        {name:'total', index:'total', sorttype:"int", align:'center'},
                        {name:'fail', index:'fail', sorttype:"int", align:'center'},
                        {name:'failPercent', index:'failPercent', sorttype:"float", align:'center'},
                        {name:'sample', index:'sample', sortable:false, align:'center'},                
                        {name:'stat', sortable:false, align:'center',width:200}
            ];
        } else {
            colnames = ['Name', 'Total Count', 'Fail Count', 'Failure%', 'Sample Link', 'Min/Max/Avg/Std(ms)'];        
            colmodels = [
                        {name:'name', index:'name', align:'center'},
                        {name:'total', index:'total', sorttype:"int", align:'center'},
                        {name:'fail', index:'fail', sorttype:"int", align:'center'},
                        {name:'failPercent', index:'failPercent', sorttype:"float", align:'center'},
                        {name:'sample', index:'sample', sortable:false, align:'center'},                
                        {name:'stat', sortable:false, align:'center',width:200}
            ];            
        }
        grid.jqGrid({
                "defaults" : {
                    //shrinkToFit:true,
                    //forceFit:true,
                },
                "datatype": "local",
                "colNames":colnames,
                "colModel":colmodels,
                "sortname":'type',
                "sortorder":'asc',
                "caption": "",
                "height": '100%',
                "autowidth": true,
                "loadComplete": function() {
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