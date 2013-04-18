<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.system.page.abtest.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.abtest.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.abtest.Model" scope="request" />
<style>
div.controls input {
	height: 30px;
}
</style>
<a:body>
   <script src="http://code.jquery.com/jquery-1.8.3.js"></script>
   <res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
   <res:useCss value="${res.css.local['bootstrap-datetimepicker.min.css']}" target="head-css" />
   <res:useCss value="${res.css.local['select2.css']}" target="head-css" />
   <res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js" />
   <res:useJs value="${res.js.local['bootstrap-datetimepicker.min.js']}" target="head-js" />
   <res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
   <div style="width: 750px; margin: 0 auto; margin-bottom: 250px;">
      <h4 style="margin: 0 auto;">创建 ABTest</h4>
      <div style="width: 90%;">
         <span style="float: right" class="label label-info"> Edit </span>
         <h5>基本信息</h5>
         <hr style="margin-top: 5px;">
         <form class="form-horizontal">
            <div class="control-group">
               <label class="control-label">测试名</label>
               <div class="controls">
                  <input type="text" placeholder="">
               </div>
            </div>
            <div class="control-group">
               <label class="control-label">简介</label>
               <div class="controls">
                  <textarea class="span6" rows="3" cols="60"></textarea>
               </div>
            </div>
            <div class="control-group">
               <label class="control-label">开始时间</label>
               <div class="controls">
                  <div id="datetimepicker1" class="input-append date">
                     <input data-format="yyyy年MM月dd日 hh点mm分" type="text"></input> <span class="add-on"> <i
                        data-time-icon="icon-time" data-date-icon="icon-calendar"> </i>
                     </span>
                  </div>
               </div>
            </div>
            <div class="control-group">
               <label class="control-label">结束时间</label>
               <div class="controls">
                  <div id="datetimepicker2" class="input-append date">
                     <input data-format="yyyy年MM月dd日 hh点mm分" type="text"></input> <span class="add-on"> <i
                        data-time-icon="icon-time" data-date-icon="icon-calendar"> </i>
                     </span>
                  </div>
               </div>
            </div>
            <div class="control-group">
               <label class="control-label">Domain</label>
               <div class="controls">
                  <select multiple="" name="e9" id="e9" style="width: 350px;" class="populate select2-offscreen" tabindex="-1">
                     <optgroup label="团购">
                        <option value="AK">Tuangou web</option>
                        <option value="HI">Tuangou mobile api</option>
                     </optgroup>
                     <optgroup label="主站">
                        <option value="CA">shop-web</option>
                     </optgroup>
                     <optgroup label="架构">
                        <option value="456">shop-web</option>
                     </optgroup>
                     <optgroup label="移动">
                        <option value="CA">shop-web</option>
                        <option value="HI">Tuangou mobile api</option>
                        <option value="321">Tuangou mobile api</option>
                        <option value="54">Tuangou mobile api</option>
                        <option value="g">Tuangou mobile api</option>
                        <option value="fsd">Tuangou mobile api</option>
                        <option value="s">Tuangou mobile api</option>
                        <option value="t">Tuangou mobile api</option>
                        <option value="yy">Tuangou mobile api</option>
                        <option value="ui">Tuangou mobile api</option>
                        <option value="kk">Tuangou mobile api</option>
                     </optgroup>
                  </select>
               </div>
            </div>
            <h5>分流配置</h5>
            <hr style="margin-top: 5px;">
            <div class="control-group">
               <label class="control-label">分流策略</label>
               <div class="controls">
                  <select>
                     <option>百分比策略</option>
                     <option>随机策略</option>
                  </select>
               </div>
            </div>
            <div class="control-group">
               <label class="control-label">策略配置信息</label>
               <div class="controls">
                  <textarea class="span6" rows="3" cols="60"></textarea>
               </div>
            </div>
         </form>
      </div>
   </div>
   <script>
				$(function() {
					$('#datetimepicker1').datetimepicker();
					$('#datetimepicker2').datetimepicker();
					$("#e9").select2();
				});
			</script>
</a:body>