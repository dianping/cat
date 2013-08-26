<%@ page contentType="text/html; charset=utf-8"%>
<div class="well sidebar-nav" >
    <ul class="nav nav-list">
      <li class='nav-header'><h4>Statistics</h4></li>
      <li id="bug" class="text-right " id="bug"><a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=view"><strong>项目异常</strong></a></li>
  	   <li id="service" class="text-right" id="service"><a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=service"><strong>服务可用性</strong></a></li>
  	   <li id="heavy" class="text-right" id="heavy"><a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=heavy"><strong>重量级访问</strong></a></li>
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

