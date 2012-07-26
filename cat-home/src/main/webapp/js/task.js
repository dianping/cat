function searchTask(domain,name,date,step){
	var type=$("#type").val();
	var status=$("#status").val();
	var name=$("#name").val();
	var url="&domain="+domain+"&name="+name+"&date="+date+"&type="+type+"&status="+status;
	window.location.href="?op=view&"+url;
}
