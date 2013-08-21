<%@ page contentType="text/html; charset=utf-8"%>
<div class="well sidebar-nav" >
         <ul class="nav nav-list">
           <li class='nav-header'><h4>Statistics</h4></li>
	       <li id="bug" class="text-right " id="bug"><a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=view"><strong>Bug</strong></a></li>
	       <li id="service" class="text-right" id="service"><a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=service"><strong>Service</strong></a></li>
	       
         </ul>
</div>
<style>
	.nav-list  li  a{
		padding:2px 15px;
	}
	.nav li  +.nav-header{
		margin-top:2px;
	}
	.nav-header{
		padding:5px 3px;
	}
	.row-fluid .span2{
		width:12%;
	}
</style>

